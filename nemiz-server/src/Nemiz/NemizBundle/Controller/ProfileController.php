<?php
namespace Nemiz\NemizBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\Route;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Serializer\Encoder\JsonEncoder;
use Symfony\Component\HttpFoundation\Response;
use Nemiz\NemizBundle\Service\UserService;
use JMS\Serializer\SerializationContext;
use Nemiz\NemizBundle\Repository\UserRepository;

class ProfileController extends Controller {
	
	/**
	 * @Route("/api/me")
	 */	
	public function meAction(Request $request) {
		$serializer = $this->get('jms_serializer');
		$data = $serializer->serialize($this->getUser(), 'json', 
			SerializationContext::create()->setGroups(array('Default')));
		
		return new Response($data, 200, array('content-type' => 'application/json'));
	}	
}