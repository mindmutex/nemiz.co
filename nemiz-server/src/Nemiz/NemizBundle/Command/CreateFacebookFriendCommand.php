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

class CreateFacebookFriendCommand extends ContainerAwareCommand {
	protected function configure() {
		$this->setName ('nemiz:friend:facebook')
			->setDescription('Creates a friend')
			->addOption('username', null, InputOption::VALUE_REQUIRED, 'User username', null)
			->addOption('facebook', null, 
					InputOption::VALUE_REQUIRED | InputOption::VALUE_IS_ARRAY, 
				'friend facebook id', null);
	}
	
	protected function execute(InputInterface $input, OutputInterface $output) {
		$doctorine = $this->getContainer()->get('doctrine.orm.entity_manager');
		$userRepository = $doctorine->getRepository(UserRepository::ENTITY);
		
		$user = $userRepository->findOneByUsername($input->getOption('username'));
		
		$friendService = $this->getContainer()->get('platform.friend.service');
		$friendService->joinWithFacebook($user, $input->getOption('facebook'));
	}
}