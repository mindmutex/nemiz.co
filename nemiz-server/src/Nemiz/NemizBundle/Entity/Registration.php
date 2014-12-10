<?php
namespace Nemiz\NemizBundle\Entity;

use JMS\Serializer\Annotation\Type;
use Symfony\Component\Validator\Constraints as Assert;

class Registration {
	/**
	 * @Type("Nemiz\NemizBundle\Entity\User")
	 * @Assert\Valid
	 * @Assert\NotBlank(message = "register.user.not_blank")
	 */
	private $user;
	
	/**
	 * @Type("Nemiz\NemizBundle\Entity\Device")
	 * @Assert\Valid
	 * @Assert\NotBlank(message = "register.device.not_blank")
	 */
	private $device;
	
	/**
	 * @Type("array<string>")
	 */
	private $friends;

	public function getUser() {
		return $this->user;
	}
	
	public function setUser(User $user) {
		$this->user = $user;
		return $this;
	}
	
	public function getDevice() {
		return $this->device;
	}
	
	public function setDevice(Device $device) {
		$this->device = $device;
		return $this;
	}
	
	public function getFriends() {
		return $this->friends;
	}
	
	public function setFriends($friends) {
		$this->friends = $friends;
		return $this;
	}
}