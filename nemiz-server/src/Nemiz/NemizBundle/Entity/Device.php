<?php
namespace Nemiz\NemizBundle\Entity;

use JMS\Serializer\Annotation\Type;
use Symfony\Component\Validator\Constraints as Assert;

class Device {
	/**
	 * @Type("string")
	 * @Assert\NotBlank(message = "device.name.not_blank")
	 * @Assert\Length(min=3, max=32, minMessage="device.name.min_length", maxMessage ="device.name.max_length")
	 */	
	private $name;
	
	/**
	 * @Type("string")
	 * @Assert\NotBlank(message = "device.type.not_blank")
	 */	
	private $type;
	
	/**
	 * @Type("string")
	 * @Assert\NotBlank(message = "device.token.not_blank")
	 */	
	private $token;
	
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
}