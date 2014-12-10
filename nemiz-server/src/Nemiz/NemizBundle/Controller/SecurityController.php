<?php

namespace Nemiz\NemizBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Security\Core\SecurityContext;

/** 
 * As from example when using authorization_code.
 */
class SecurityController extends Controller {
	public function loginAction(Request $request) {
		$session = $request->getSession();
		
		$authErrorKey = SecurityContext::AUTHENTICATION_ERROR;
		if ($request->attributes->has($authErrorKey)) {
			$error = $request->attributes->get($authErrorKey);
		} elseif ($session !== null && $session->has($authErrorKey)) {
			$error = $session->get($authErrorKey);
			$session->remove ($authErrorKey);
		} else {
			$error = '';
		}
		
		if ($error) {
			$error = $error->getMessage();
		}
		
		$lastUsername = '';
		if ($session !== null) {
			$lastUsername = $session->get (SecurityContext::LAST_USERNAME);
		}
		
		return $this->render ('NemizBundle:Security:login.html.twig', 
			array ('last_username' => $lastUsername, 'error' => $error ));
	}
	
	public function loginCheckAction(Request $request) {
	}
}