<?php

namespace Nemiz\NemizBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * Nemiz\NemizBundle\Entity\Activity
 *
 * @ORM\Table(name="nemiz_activity")
 * @ORM\Entity(repositoryClass="Nemiz\NemizBundle\Repository\ActivityRepository")
 */
class Activity {
	/**
	 * @ORM\Column(type="integer")
	 * @ORM\Id
	 * @ORM\GeneratedValue(strategy="AUTO")
	 */
	private $id;
	
	/**
	 * @ORM\ManyToOne(targetEntity="User")
	 */
	private $user;
	
	/**
	 * @ORM\ManyToOne(targetEntity="User")
	 */
	private $friend;
	
	/**
	 * @ORM\Column(type="datetime")
	 */
	private $dateCreated;
	
	
	private $received = false;
	
	/**
	 * Get id
	 *
	 * @return integer
	 */
	public function getId() {
		return $this->id;
	}
	
	/**
	 * Set dateCreated
	 *
	 * @param \DateTime $dateCreated        	
	 * @return Activity
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
	 * @return Activity
	 */
	public function setUser(\Nemiz\NemizBundle\Entity\User $user = null) {
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
	 * @return Activity
	 */
	public function setFriend(\Nemiz\NemizBundle\Entity\User $friend = null) {
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
	
	public function getReceived() {
		return $this->received;
	}
	
	public function setReceived($received) {
		$this->received = $received;
		return $this;
	}
}
