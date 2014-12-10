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
use Nemiz\NemizBundle\Repository\FriendshipRepository;
use Nemiz\NemizBundle\Repository\UserRepository;

class FriendsController extends Controller {
	
	/**
	 * @Route("/api/friends")
	 * @Method("GET")
	 */
	public function indexAction(Request $request) {
		$serializer = $this->get('jms_serializer');
		$repository = $this->getDoctrine()->getRepository(FriendshipRepository::ENTITY);
		
		$data = $serializer->serialize(
				$repository->getFriends($this->getUser()), "json", 
			SerializationContext::create()->setGroups(array('Default')));
		
		return new Response($data, 200, array('content-type' => 'application/json'));
	}
	
	/**
	 * @Route("/api/friends")
	 * @Method("POST")
	 */	
	public function pokeAction(Request $request) {
		$serializer = $this->get('jms_serializer');
		$data = $serializer->deserialize($request->getContent(), 'array', 'json');
		
		if (isset($data['friend'])) {
			$repository = $this->getDoctrine()->getRepository(UserRepository::ENTITY);
			$friend = $repository->findOneById($data['friend']);
			
			// poke friend
			$friendService = $this->get('platform.friend.service');
			$friendService->poke($this->getUser(), $friend);
			
			// log activity
			$activityService = $this->get('platform.activity.service');
			$activityService->log($this->getUser(), $friend);
			
			return new Response();
		}
		return new Response('', Response::HTTP_BAD_REQUEST);
	}
}