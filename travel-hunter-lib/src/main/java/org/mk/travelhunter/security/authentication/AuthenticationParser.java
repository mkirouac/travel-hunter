package org.mk.travelhunter.security.authentication;

import org.springframework.security.core.Authentication;

public interface AuthenticationParser<T extends Authentication> {

	Class<T> getSupportedAuthenticationType();
	
	String getUserName(Authentication authentication);
	
	String getAuthenticationProvider(Authentication authentication);
	
}
