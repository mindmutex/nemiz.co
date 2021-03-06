<?php
namespace Nemiz\NemizBundle\Entity;

use FOS\OAuthServerBundle\Entity\AuthCode as BaseAuthCode;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\UserInterface;
use FOS\OAuthServerBundle\Model\ClientInterface;

/**
 * @ORM\Table(name="nemiz_oauth_auth_codes")
 * @ORM\Entity
 */
class AuthCode extends BaseAuthCode {
	/**
	 * @ORM\Id
	 * @ORM\Column(type="integer")
	 * @ORM\GeneratedValue(strategy="AUTO")
	 */
	protected $id;
	
	/**
	 * @ORM\ManyToOne(targetEntity="Client")
	 * @ORM\JoinColumn(nullable=false)
	 */
	protected $client;
	
	/**
	 * @ORM\ManyToOne(targetEntity="User")
	 */
	protected $user;
	
	/**
	 * Get id
	 *
	 * @return integer
	 */
	public function getId() {
		return $this->id;
	}
	
	/**
	 * Set client
	 *
	 * @param \FOS\OAuthServerBundle\Model\ClientInterface $client        	
	 * @return AuthCode
	 */
	public function setClient(ClientInterface $client) {
		$this->client = $client;
		
		return $this;
	}
	
	/**
	 * Get client
	 *
	 * @return \FOS\OAuthServerBundle\Model\ClientInterface
	 */
	public function getClient() {
		return $this->client;
	}
	
	/**
	 * Set user
	 *
	 * @param \Symfony\Component\Security\Core\User\UserInterface $user        	
	 * @return AuthCode
	 */
	public function setUser(UserInterface $user = null) {
		$this->user = $user;
		
		return $this;
	}
	
	/**
	 * Get user
	 *
	 * @return \Symfony\Component\Security\Core\User\UserInterface
	 */
	public function getUser() {
		return $this->user;
	}
}
