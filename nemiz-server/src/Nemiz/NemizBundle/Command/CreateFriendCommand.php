<?php
namespace Nemiz\NemizBundle\Command;

use Symfony\Bundle\FrameworkBundle\Command\ContainerAwareCommand;
use Symfony\Component\Console\Input\InputArgument;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Nemiz\NemizBundle\Entity\User;
use Nemiz\NemizBundle\Entity\Friendship;
use Nemiz\NemizBundle\Service\UserService;
use Nemiz\NemizBundle\Repository\UserRepository;

class CreateFriendCommand extends ContainerAwareCommand {
	protected function configure() {
		$this->setName ('nemiz:friend:create')
			->setDescription('Creates a friend')
			->addOption('username', 
				null, InputOption::VALUE_REQUIRED, 'User username', null)
			->addOption('friend', null, InputOption::VALUE_REQUIRED, '`Friend username', null);
	}
	
	protected function execute(InputInterface $input, OutputInterface $output) {
		$doctorine = $this->getContainer()->get('doctrine.orm.entity_manager');
		$userRepository = $doctorine->getRepository(UserRepository::ENTITY);
		
		$user = $userRepository->findOneByUsername($input->getOption('username'));
		if ($user == null) {
			$user = $userRepository->findOneByEmail($input->getOption('username'));
		}
		
		$friend = $userRepository->findOneByUsername($input->getOption('friend'));
		if ($friend == null) {
			$friend = $userRepository->findOneByEmail($input->getOption('friend'));
		}
		
		$friendService = $this->getContainer()->get('platform.friend.service');
		$friendService->joinUsers($user, $friend);
	}
}