<?php

namespace Nemiz\NemizBundle\Tests\Controller;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;
use Nemiz\NemizBundle\Entity;
use Nemiz\NemizBundle\Entity\Registration;
use Nemiz\NemizBundle\Entity\Device;
use Nemiz\NemizBundle\Entity\User;
use JMS\Serializer\SerializationContext;
use Nemiz\NemizBundle\Entity\Client;

class FriendsControllerTest extends WebTestCase {
	public function testGet() {
		$client = static::createClient();
		$container = $client->getContainer();
		
		$object = array('friend' => 2);
		
		$serializer = $container->get('jms_serializer');
		$data = $serializer->serialize($object, 'json');
		
		$client->request('POST', '/api/friends?access_token=NTU3YmQ0OWY3ZGYwYzQ0NWUwNGJhMjFmNzlmN2RkMmE0Mjc0YmY4M2M4N2JmOGJmZDA4Y2U0MmVkZmY5MmJjZA', 
			array(), 
			array(), 
			array('CONTENT_TYPE' => 'application/json'), $data);
		
		$response = json_decode($client->getResponse()->getContent());
		var_dump($response);
	}
}
