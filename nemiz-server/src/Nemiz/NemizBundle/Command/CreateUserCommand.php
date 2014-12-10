<?php
namespace Nemiz\NemizBundle\Command;

use Symfony\Bundle\FrameworkBundle\Command\ContainerAwareCommand;
use Symfony\Component\Console\Input\InputArgument;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Nemiz\NemizBundle\Entity\User;

class CreateUserCommand extends ContainerAwareCommand {
	protected function configure() {
		$this->setName ('nemiz:user:create')
			->setDescription('Creates a new user')
			->addOption('username', null, InputOption::VALUE_REQUIRED, 'Sets the user username', null)
			->addOption('password', null, InputOption::VALUE_REQUIRED, 'Sets the user password', null)
			->addOption('name', null, InputOption::VALUE_REQUIRED, 'Sets the user name', null)
			->addOption('email', null, InputOption::VALUE_REQUIRED, 'Sets the user e-mail', null)
			->addOption('facebook', null, InputOption::VALUE_REQUIRED, 'Sets the user facebook', null);
	}
	
	protected function execute(InputInterface $input, OutputInterface $output) {
		$user = new User();
		$user->setUsername($input->getOption("username"));
		$user->setPassword($input->getOption('password'));
		$user->setName($input->getOption("name"));
		$user->setEmail($input->getOption("email"));
		$user->setFacebook($input->getOption("facebook"));
		
		$userService = $this->getContainer()->get('platform.user.service');
		$userService->create($user);
		
		$output->writeln("OK, userId=" . $user->getId());
	}
}