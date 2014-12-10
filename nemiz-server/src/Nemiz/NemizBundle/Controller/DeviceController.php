<?php
namespace Nemiz\NemizBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;

use Sensio\Bundle\FrameworkExtraBundle\Configuration\Route;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\Method;

use Nemiz\NemizBundle\Service\FriendService;
use Nemiz\NemizBundle\Service\UserService;
use JMS\Serializer\SerializationContext;
use Nemiz\NemizBundle\Entity\Device;
use Nemiz\NemizBundle\Repository\ClientRepository;
use Symfony\Component\HttpFoundation\JsonResponse;

class DeviceController extends Controller {
	
	/**
	 * Adds ability for the client to register new device.
	 * 
	 * @Route("/api/devices")
	 * @Method("POST")
	 */
	public function createDevice(Request $request) {
		$serializer = $this->get('jms_serializer');
		$device = $serializer->deserialize($request->getContent(), get_class(new Device()), 'json');
		
		$errors = $this->get('validator')->validate($device);
		if (count($errors) > 0) {
			return new Response(
				$serializer->serialize($errors, 'json'),
				Response::HTTP_BAD_REQUEST);
		}		
		
		$repository = $this->getDoctrine()->getRepository(ClientRepository::ENTITY);
		$service = $this->get('platform.client.service');
				
		$client = $repository->findOneBy(
			array('owner' => $this->getUser(), 'name' => $device->getName()));
		
		if ($client == null) {
			$client = $service->createDevice($this->getUser(), $device);
		} else {
			$client->setToken($device->getToken());
				
			$service->updateDevice($this->getUser(), $client);
		}
		return new JsonResponse(array('clientId' => $client->getPublicId(),
				'clientSecret' => $client->getSecret(), 
			'userId' => $client->getOwner()->getId()));		
	}
	
}