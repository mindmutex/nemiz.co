<?php

namespace Nemiz\NemizBundle\Service;

use Nemiz\NemizBundle\Entity\User;
use Doctrine\ORM\EntityManager;
use Monolog\Logger;
use Nemiz\NemizBundle\Entity\Friendship;
use Nemiz\NemizBundle\Repository\FriendshipRepository;
use JMS\Serializer\SerializationContext;

class FriendService {
	
	private $entityManager;
	private $logger;
	private $sns;
	private $serializer;
	
	public function __construct(EntityManager $entityManager, $sns, $serializer, Logger $logger) {
		$this->entityManager = $entityManager;
		$this->logger = $logger;
		$this->serializer = $serializer;
		$this->sns = $sns;
	}
	
	public function poke(User $user, User $friend) {
		if ($this->isFriend($user, $friend)) {
			// check if the friend is friends with user otherwise the friend cannot respond to poke
			if (!$this->isFriend($friend, $user)) {
				$this->joinUsers($friend, $user);
			}
			
			$data = $this->serializer->serialize($user, 'json',
				SerializationContext::create()->setGroups(array('Default')));
			
			$clientRepository = $this->entityManager->getRepository("NemizBundle:Client");
			$devices = $clientRepository->findByOwner($friend);
			
			foreach ($devices as $device) {
				$this->sns->publish(array(
					'TargetArn' => $device->getArn(), 
					'Message' => $data
				));
			}
		}
	}
	
	public function isFriend(User $user, User $friend) {
		$repository = $this->entityManager->getRepository(FriendshipRepository::ENTITY);
		$friendship = $repository->findOneBy(array(
				'user' => $user, 'friend' => $friend
		));
		return $friendship != null;
	}
	
	public function joinUsers(User $user, User $friend) {
		$dateCreated = new \DateTime();
		 
		if (!$this->isFriend($user, $friend)) {
			$friendship = new Friendship();
			$friendship->setUser($user);
			$friendship->setFriend($friend);
			$friendship->setDateCreated($dateCreated);
			$this->entityManager->persist($friendship);
		}
		
		if (!$this->isFriend($friend, $user)) { 
			$friendship = new Friendship();
			$friendship->setUser($friend);
			$friendship->setFriend($user);
			$friendship->setDateCreated($dateCreated);
			$this->entityManager->persist($friendship);
		}
		
		$this->entityManager->flush();
	}
	
	public function joinWithFacebook(User $user, $friends = array()) {
		$repository = $this->entityManager->getRepository(UserService::ENTITY);
		if (!empty($friends)) {
			$builder = $repository->createQueryBuilder('u');
			$query = $builder->add('where', $builder->expr()->in('u.facebook', $friends))->getQuery();
			
			foreach ($query->getResult() as $friend) {
				try {
					$this->joinUsers($user, $friend);
				} catch (\Exception $e) {
					$this->logger->error("Failed to add facebook user", $e);
				}
			}
		}
	}
}