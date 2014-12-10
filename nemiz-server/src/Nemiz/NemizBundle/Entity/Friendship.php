<?php

namespace Nemiz\NemizBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * Nemiz\NemizBundle\Entity\Friendship
 *
 * @ORM\Table(name="nemiz_friendships")
 * @ORM\Entity(repositoryClass="Nemiz\NemizBundle\Repository\FriendshipRepository")
 */
class Friendship {
	/**
	 * @ORM\Column(type="integer")
	 * @ORM\Id
	 * @ORM\GeneratedValue(strategy="AUTO")
	 */
	private $id;
		
	/**
	 * @ORM\ManyToOne(targetEntity="User")
	 */
	protected $user;
	
	/**
	 * @ORM\ManyToOne(targetEntity="User")
	 */
	protected $friend;
	
	/**
	 * @ORM\Column(name="date_created", type="datetime")
	 */
	private $dateCreated;
	
	public function getId() {
		return $this->id;
	}
	
	public function setId($id) {
		$this->id = $id;
		return $this;
	}
	
	/**
	 * Set dateCreated
	 *
	 * @param \DateTime $dateCreated        	
	 * @return Friendship
	 */
	public function setDateCreated($dateCreated) {
		$this->dateCreated = $dateCreated;
		return $this;
	}
	
	/**
	 * Get dateCreated
	 *
	 * @return \DateTime
	 */
	public function getDateCreated() {
		return $this->dateCreated;
	}
	
	/**
	 * Set user
	 *
	 * @param \Nemiz\NemizBundle\Entity\User $user        	
	 * @return Friendship
	 */
	public function setUser(User $user = null) {
		$this->user = $user;
		return $this;
	}
	
	/**
	 * Get user
	 *
	 * @return \Nemiz\NemizBundle\Entity\User
	 */
	public function getUser() {
		return $this->user;
	}
	
	/**
	 * Set friend
	 *
	 * @param \Nemiz\NemizBundle\Entity\User $friend        	
	 * @return Friendship
	 */
	public function setFriend(User $friend = null) {
		$this->friend = $friend;
		
		return $this;
	}
	
	/**
	 * Get friend
	 *
	 * @return \Nemiz\NemizBundle\Entity\User
	 */
	public function getFriend() {
		return $this->friend;
	}
}
