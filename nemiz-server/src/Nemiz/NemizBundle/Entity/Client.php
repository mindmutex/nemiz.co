<?php

namespace Nemiz\NemizBundle\Entity;

use FOS\OAuthServerBundle\Entity\Client as BaseClient;
use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Table(name="nemiz_oauth_clients")
 * @ORM\Entity(repositoryClass="Nemiz\NemizBundle\Repository\ClientRepository")
 */
class Client extends BaseClient {
	/**
	 * @ORM\Id
	 * @ORM\Column(type="integer")
	 * @ORM\GeneratedValue(strategy="AUTO")
	 */
	protected $id;
	
	/**
	 * @ORM\ManyToOne(targetEntity="User")
	 */
	private $owner;	
	
	/**
	 * @ORM\ManyToOne(targetEntity="User")
	 */
	private $impersonate;	
	
	/**
	 * @ORM\Column(type="string", length=60)
	 */
	private $name;
	
	/**
	 * @ORM\Column(type="string", length=32)
	 */
	private $type;
	
	/**
	 * @ORM\Column(type="string", length=256, nullable=true)
	 */
	private $token;
	
	/**
	 * @ORM\Column(type="string", length=256, nullable=true)
	 */
	private $arn;	
	
	/**
	 * @ORM\Column(type="datetime")
	 */
	private $dateCreated;
	
	public function __construct() {
		parent::__construct ();
	}
	
	/**
	 * Get id
	 *
	 * @return integer
	 */
	public function getId() {
		return $this->id;
	}
	
	public function getOwner() {
		return $this->owner;
	}
	
	public function setOwner($owner) {
		$this->owner = $owner;
		return $this;
	}
		
	public function getImpersonate() {
		return $this->impersonate;
	}
	
	public function setImpersonate($impersonate) {
		$this->impersonate = $impersonate;
		return $this;
	}
	
	public function getName() {
		return $this->name;
	}
	
	public function setName($name) {
		$this->name = $name;
		return $this;
	}
	
	public function getType() {
		return $this->type;
	}
	
	public function setType($type) {
		$this->type = $type;
		return $this;
	}
	
	public function getToken() {
		return $this->token;
	}
	
	public function setToken($token) {
		$this->token = $token;
		return $this;
	}
	
	public function getArn() {
		return $this->arn;
	}
	
	public function setArn($arn) {
		$this->arn = $arn;
		return $this;
	}	
	
	public function getDateCreated() {
		return $this->dateCreated;
	}
	
	public function setDateCreated($dateCreated) {
		$this->dateCreated = $dateCreated;
		return $this;
	}

	
}
