package org.mk.travelhunter.security;

import org.mk.travelhunter.security.authentication.AuthenticationParser;
import org.springframework.security.core.Authentication;

class NullAuthenticationParser implements AuthenticationParser<Authentication> {
	@Override
	public Class<Authentication> getSupportedAuthenticationType() {
		return null;
	}

	@Override
	public String getUserName(Authentication authentication) {
		return SpringSecurityController.GUEST;
	}

	@Override
	public String getAuthenticationProvider(Authentication authentication) {
		return null;
	}
}