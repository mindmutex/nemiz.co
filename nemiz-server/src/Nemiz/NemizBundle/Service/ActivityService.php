<?php 

namespace Nemiz\NemizBundle\Service;

use Nemiz\NemizBundle\Entity\User;
use Doctrine\ORM\EntityManager;
use Monolog\Logger;
use Nemiz\NemizBundle\Entity\Friendship;
use Nemiz\NemizBundle\Repository\FriendshipRepository;
use JMS\Serializer\SerializationContext;
use Nemiz\NemizBundle\Entity\Activity;

class ActivityService {

	private $entityManager;
	private $logger;

	public function __construct(EntityManager $entityManager, Logger $logger) {
		$this->entityManager = $entityManager;
		$this->logger = $logger;
	}
	
	public function log(User $user, User $friend) {
		$activity = new Activity();
		$activity->setUser($user); 
		$activity->setFriend($friend);
		$activity->setDateCreated(new \DateTime());	
	
		$this->entityManager->persist($activity);
		$this->entityManager->flush();
	}
}
