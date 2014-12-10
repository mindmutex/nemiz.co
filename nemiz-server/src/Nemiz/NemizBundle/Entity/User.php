<?php
namespace Nemiz\NemizBundle\Entity;

use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;

use Doctrine\ORM\Mapping as ORM;
use JMS\Serializer\Annotation\Expose;
use JMS\Serializer\Annotation\ExclusionPolicy;
use JMS\Serializer\Annotation\Groups;

/**
 * Nemiz\NemizBundle\Entity\User
 * 
 * @ORM\Table(name="nemiz_users")
 * @ORM\Entity(repositoryClass="Nemiz\NemizBundle\Repository\UserRepository")
 * @ExclusionPolicy("all")
 * @UniqueEntity(fields = "email", message = "user.email.unique")
 * @UniqueEntity(fields = "username", message = "user.username.unique")
 */
class User implements UserInterface {
	
	/**
	 * @ORM\Column(type="integer")
	 * @ORM\Id
	 * @ORM\GeneratedValue(strategy="AUTO")
	 * @Expose
	 */
	private $id;
	
	/**
	 * @ORM\Column(type="string", length=32, unique=true, nullable=true)
	 * @Expose
	 */
	private $username;
	
	/**
	 * @ORM\Column(type="string", length=60)
	 * @Assert\NotBlank(message = "user.password.not_blank")
	 * @Groups({"register"})
	 * @Expose
	 */
	private $password;
	
	/** 
	 * @ORM\Column(type="string", length=64)
	 */
	private $salt;
	
	/** 
	 * @ORM\Column(type="string", length=32)
	 * @Assert\NotBlank(message = "user.name.not_blank")
	 * @Assert\Length(min=3, max=32, minMessage="user.name.min_length", maxMessage ="user.name.max_length")
	 * @Expose
	 */
	private $name;
	
	/**
	 * @ORM\Column(type="string", length=32)
	 * @Assert\NotBlank(message = "user.facebook.not_blank")
	 * @Assert\Length(min=3, max=32, minMessage="user.facebook.min_length", maxMessage ="user.facebook.max_length")
	 * @Expose
	 * @Groups({"register"})
	 */
	private $facebook;	
		
	/**
	 * @ORM\Column(type="string", length=64, unique=true)
	 * @Assert\NotBlank(message = "user.email.not_blank")
	 * @Assert\Email()
	 * @Assert\Length(min=3, max=64, minMessage="user.email.min_length", maxMessage ="user.email.max_length")
	 * @Expose
	 */
	private $email;
	
	/**
	 * @ORM\Column(name="is_active", type="boolean")
	 * @Expose
	 */
	private $enabled;
	
	/**
	 * @ORM\Column(name="date_created", type="datetime")
	 * @Expose
	 */
	private $dateCreated;
	
	public function __construct() {
		$this->setEnabled(true);
		$this->setSalt(md5(uniqid(null, true)));
	}
	
	public function getId() {
		return $this->id;
	}
	
	public function setId($id) {
		$this->id = $id;
		return $this;
	}
	
	public function getUsername() {
		return $this->username;
	}
	
	public function setUsername($username) {
		$this->username = $username;
		return $this;
	}
	
	public function getPassword() {
		return $this->password;
	}
	
	public function setPassword($password) {
		$this->password = $password;
		return $this;
	}
	
	public function getSalt() {
		return $this->salt;
	}
	
	public function setSalt($salt) {
		$this->salt = $salt;
		return $this;
	}
	
	public function getName() {
		return $this->name;
	}
	
	public function setName($name) {
		$this->name = $name;
		return $this;
	}
	
	public function getEmail() {
		return $this->email;
	}
	
	public function setEmail($email) {
		$this->email = $email;
		return $this;
	}
	
	public function getEnabled() {
		return $this->enabled;
	}
	
	public function setEnabled($enabled) {
		$this->enabled = $enabled;
		return $this;
	}
	
	public function getRoles() {
		return array('ROLE_USER');
	}
	
	public function eraseCredentials() {
	}
	
	public function getDateCreated() {
		return $this->dateCreated;
	}
	
	public function setDateCreated(\DateTime $dateCreated) {
		$this->dateCreated = $dateCreated;
		return $this;
	}
	public function getFacebook() {
		return $this->facebook;
	}
	
	public function setFacebook($facebook) {
		$this->facebook = $facebook;
		return $this;
	}
	
	
	
}
