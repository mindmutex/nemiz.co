<?php

namespace Nemiz\NemizBundle\Tests\Controller;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;
use Nemiz\NemizBundle\Entity;
use Nemiz\NemizBundle\Entity\Registration;
use Nemiz\NemizBundle\Entity\Device;
use Nemiz\NemizBundle\Entity\User;
use JMS\Serializer\SerializationContext;
use Nemiz\NemizBundle\Entity\Client;

class DefaultControllerTest extends WebTestCase {
	
	
	protected function setUp() {	
		
	}
	
	public function testRegister() {
		$client = static::createClient();
		$container = $client->getContainer();
		
		$mock = $this->getMockBuilder(
				$container->getParameter('platform.user.service.class'))
			->disableOriginalConstructor()
			->getMock();
		
		$mock->expects($this->once())
			->method('createWithDevice')
			->will($this->returnValue(new Client()));
		
		$container->set("platform.user.service", $mock);
		
		$device = new Device();
		$device->setName("DeviceName");
		$device->setToken("CloudToken");
		$device->setType("android");
		
		$user = new User();
		$user->setName("Name");
		$user->setUsername(md5(time()));
		$user->setFacebook("fbid");
		$user->setEmail("email@local.ho");
		$user->setPassword("password");
		
		$registration = new Registration();
		$registration->setUser($user);
		$registration->setDevice($device);
		
		$serializer = $container->get('jms_serializer');
		$data = $serializer->serialize($registration, 'json', 
			SerializationContext::create()->setGroups(array('Default', 'register')));

		$client->request('POST', '/register', 
				array(), array(), array('CONTENT_TYPE' => 'application/json'), 
			$data);
		
		$response = json_decode($client->getResponse()->getContent());

		$this->assertObjectHasAttribute('client_id', $response);
		$this->assertObjectHasAttribute('client_secret', $response);
	}
}
