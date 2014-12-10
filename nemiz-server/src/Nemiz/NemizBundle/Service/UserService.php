<?php
namespace Nemiz\NemizBundle\Service;

use Symfony\Component\Security\Core\Encoder\EncoderFactory;
use Doctrine\ORM\EntityManager;
use Nemiz\NemizBundle\Entity\User;
use Nemiz\NemizBundle\Entity\Device;
use FOS\OAuthServerBundle\Entity\ClientManager;
use Monolog\Logger;

class UserService {
	private $entityManager;
	private $clientService;
	private $encoderFactory;
	
	private $logger;
	
	public function __construct(EntityManager $entityManager, 
			EncoderFactory $encoderFactory, ClientService $clientService, Logger $logger) {
		
		$this->entityManager = $entityManager;
		$this->encoderFactory = $encoderFactory;
		$this->clientService = $clientService;
		$this->logger = $logger;
	}
	
	public function create(User $user) {
		$this->logger->info(sprintf('Adding new user %s (%s)', $user->getName(), $user->getEmail()));
		$encoder = $this->encoderFactory->getEncoder($user);
		
		$user->setDateCreated(new \DateTime());
		$user->setEnabled(true);
		$user->setSalt(md5(time()));
		$user->setPassword($encoder->encodePassword($user->getPassword(), $user->getSalt()));
		
		$this->entityManager->persist($user);
		$this->entityManager->flush();
		
		return $user;
	}
	
	public function createWithDevice(User $user, Device $device) {
		$this->entityManager->beginTransaction();
		try {
			$user = $this->create($user);
			$client = $this->clientService->createDevice($user, $device);
			
			$this->entityManager->getConnection()->commit();
			return $client;
		} catch (Exception $e) {
			$this->entityManager->getConnection()->rollBack();
			throw $e;
		}
	}
}