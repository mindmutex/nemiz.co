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
use Nemiz\NemizBundle\Repository\ActivityRepository;
use Doctrine\ORM\Tools\Pagination\Paginator;

class ActivityController extends Controller {
	/**
	 * @Route("/api/activity")
	 * @Method("GET")
	 */	
	public function handleGet(Request $request) {
		$repository = $this->getDoctrine()->getRepository(ActivityRepository::ENTITY);
		
		$offset = $request->query->get("offset", 0);
		$query = $repository->createQueryBuilder('a')
				->where('a.user = :user or a.friend = :user order by a.dateCreated desc')
				->setFirstResult($offset)
				->setMaxResults(20)
				->setParameter('user', $this->getUser())
			->getQuery();

		$result = $query->getResult();
		
		$temp = array();
		foreach ($result as $activity) {
			$activity->setReceived($activity->getUser()->getId() != $this->getUser()->getId());
		}
		
		$serializer = $this->get('jms_serializer');
		$data = $serializer->serialize($result, 'json',
			SerializationContext::create()->setGroups(array('Default')));
		
		return new Response($data, 200, array('content-type' => 'application/json'));
	}
}
