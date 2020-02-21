package org.mk.travelhunter.security;

public interface SecurityController {

	boolean isUserAuthenticated();
	
	String getUserName();
	
	String getAuthenticationProvider();
	
}
