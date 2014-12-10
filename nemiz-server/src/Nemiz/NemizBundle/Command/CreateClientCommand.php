<?php

namespace Nemiz\NemizBundle\Command;

use Symfony\Bundle\FrameworkBundle\Command\ContainerAwareCommand;
use Symfony\Component\Console\Input\InputArgument;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

class CreateClientCommand extends ContainerAwareCommand {
	protected function configure() {
		$this->setName('nemiz:oauth:client:create')
			->setDescription('Creates a new client')
			->addOption('user', null, InputOption::VALUE_OPTIONAL, 'Sets user to impersonate')
			->addOption('name', null, InputOption::VALUE_REQUIRED, 'Sets name of client')
			->addOption('type', null, InputOption::VALUE_REQUIRED, 'Sets type of client')
			->addOption('redirect-uri', 
					null, InputOption::VALUE_REQUIRED | InputOption::VALUE_IS_ARRAY, 
				'Sets redirect uri for client. Use this option multiple times to set multiple redirect URIs.', null)
			->addOption('grant-type', 
					null, InputOption::VALUE_REQUIRED | InputOption::VALUE_IS_ARRAY, 
				'Sets allowed grant type for client. Use this option multiple times to set multiple grant types..', null);
	}
	
	protected function execute(InputInterface $input, OutputInterface $output) {
		$clientManager = $this->getContainer()->get('fos_oauth_server.client_manager.default');
		
		$doctorine = $this->getContainer()->get('doctrine.orm.entity_manager');
		$userRepository = $doctorine->getRepository('NemizBundle:User');
		
		$client = $clientManager->createClient();
		$client->setName($input->getOption("name"));
		$client->setType($input->getOption("type"));
		$client->setRedirectUris($input->getOption('redirect-uri'));
		$client->setAllowedGrantTypes($input->getOption('grant-type'));
		$client->setDateCreated(new \DateTime());
		
		$username = $input->getOption('user');
		if (!empty($username)) {
			$user = $userRepository->findOneBy(array('username' => $username));
			
			$client->setImpersonate($user);
			$client->setOwner($user);
		}
		$clientManager->updateClient($client);
		$output->writeln(sprintf(
				'Added a new client with public id <info>%s</info>, secret <info>%s</info>', 
			$client->getPublicId(), $client->getSecret()));
	}
}