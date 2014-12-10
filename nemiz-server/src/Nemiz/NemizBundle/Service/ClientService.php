<?php
namespace Nemiz\NemizBundle\Service;


use Doctrine\ORM\EntityManager;
use Monolog\Logger;
use Nemiz\NemizBundle\Entity\Device;
use Nemiz\NemizBundle\Entity\User;
use FOS\OAuthServerBundle\Entity\ClientManager;
use Aws\Sns\SnsClient;
use Aws\Sns\Exception\InvalidParameterException;
use Nemiz\NemizBundle\Entity\Client;

class ClientService {
	private $entityManager;
	private $logger;
	private $sns;
	private $platformArn;
	private $clientManager;
	
	public function __construct(EntityManager $entityManager, ClientManager $clientManager, SnsClient $sns, $platformArn, Logger $logger) {
		$this->entityManager = $entityManager;
		$this->clientManager = $clientManager;
		$this->sns = $sns;
		$this->platformArn = $platformArn;
		$this->logger = $logger;
	}
	
	public function createDevice(User $user, Device $device) {
		$arn = $this->sns->createPlatformEndpoint(array(
			'PlatformApplicationArn' => $this->platformArn,
			'Token' => $device->getToken(),
			'CustomUserData' => $user->getEmail()
		));
		$arn = $arn->get("EndpointArn");
				
		$client = $this->clientManager->createClient();
		$client->setName($device->getName());
		$client->setType($device->getType());
		$client->setToken($device->getToken());
		$client->setArn($arn);
		$client->setAllowedGrantTypes(array('client_credentials'));
		$client->setOwner($user);
		$client->setImpersonate($user);
		$client->setDateCreated(new \DateTime());
		
		$this->clientManager->updateClient($client);
		
		$this->logger->info(sprintf(
				'Added a new client with public id %s, secret %s',
			$client->getPublicId(), $client->getSecret()));		
		
		return $client;
	}
	
	public function updateDevice(User $user, Client $client) {
		$this->logger->info(sprintf('Updating device %s', $client->getPublicId()));
		
		$arn = $this->sns->createPlatformEndpoint(array(
				'PlatformApplicationArn' => $this->platformArn,
				'Token' => $client->getToken(),
				'CustomUserData' => $user->getEmail()
		));

		$client->setArn($arn->get("EndpointArn"));
		
		$this->clientManager->updateClient($client);
	}
}