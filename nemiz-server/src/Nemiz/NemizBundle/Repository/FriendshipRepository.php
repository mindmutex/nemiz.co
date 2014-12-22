<?php

namespace Nemiz\NemizBundle\Repository;

use Doctrine\ORM\EntityRepository;
use Nemiz\NemizBundle\Entity\User;
use Nemiz\NemizBundle\Service\UserService;
use Nemiz\NemizBundle\Service\FriendService;

/**
 * FriendshipRepository
 *
 * This class was generated by the Doctrine ORM. Add your own custom
 * repository methods below.
 */
class FriendshipRepository extends EntityRepository {
	const ENTITY = "NemizBundle:Friendship";
	
	public function getFriends(User $user) {
		
		return $this->_em->createQueryBuilder()
			->from(FriendshipRepository::ENTITY, 'f')
			->from(UserRepository::ENTITY, 'u')
			->select('u')
			->where('u = f.friend and f.user = :user')
			->orderBy('u.name', 'ASC')
			->setParameter('user', $user)
			->getQuery()
			->getResult();
	}
}
