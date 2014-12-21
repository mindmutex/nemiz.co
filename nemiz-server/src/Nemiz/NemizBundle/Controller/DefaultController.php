<?php
namespace Nemiz\NemizBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\Serializer\Encoder\JsonDecode;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpKernel\Exception\HttpException;

use Sensio\Bundle\FrameworkExtraBundle\Configuration\Route;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\Method;

use Nemiz\NemizBundle\Entity\User;
use Nemiz\NemizBundle\Entity\Registration;

class DefaultController extends Controller {
	/**
	 * @Route("/")
	 */
	public function indexAction(Request $request) {
		return $this->render ('NemizBundle:Default:index.html.twig');
	}
	
	/**
	 * @Route("/report")
	 * @Method("POST")
	 */
	public function reportAction(Request $request) {
		$snsClient = $this->get("aws.sns.service");
		
		$arn = $this->container->getParameter('aws.report_arn');
		$content = $request->getContent();
		
		$result = $snsClient->publish(
			array('TargetArn' => $arn, 'Message' => $content));
	}
	
	/**
	 * @Route("/register")
	 * @Method("POST")
	 */	
	public function registerAction(Request $request) {
		$serializer = $this->get('jms_serializer');
		$content = $request->getContent(); 
		
		$register = $serializer->deserialize(
			$content, get_class(new Registration()), 'json');
		
		$errors = $this->get('validator')->validate($register);
		if (count($errors) > 0) {
			return new Response(
					$serializer->serialize($errors, 'json'), 
				Response::HTTP_BAD_REQUEST);
		}
		$userService = $this->get('platform.user.service');
		$client = $userService->createWithDevice($register->getUser(), $register->getDevice());
		
		$friendService = $this->get('platform.friend.service');
		$friendService->joinWithFacebook(
			$client->getOwner(), $register->getFriends());
		
		return new JsonResponse(array(
			'userId' => $client->getOwner()->getId(),
			'clientId' => $client->getPublicId(), 
			'clientSecret' => $client->getSecret()));
	}
}
