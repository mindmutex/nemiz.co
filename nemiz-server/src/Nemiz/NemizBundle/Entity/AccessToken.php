<?php
namespace Nemiz\NemizBundle\Entity;

use FOS\OAuthServerBundle\Entity\AccessToken as BaseAccessToken;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\UserInterface;
use FOS\OAuthServerBundle\Model\ClientInterface;

/**
 * @ORM\Table(name="nemiz_oauth_access_tokens")
 * @ORM\Entity
 */
class AccessToken extends BaseAccessToken {
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
	 * @return AccessToken
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
	 * @return AccessToken
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
		if ($this->user == null 
				&& $this->client instanceof Client) {
			$this->user = $this->client->getImpersonate();
		}
		return $this->user;
	}
}
