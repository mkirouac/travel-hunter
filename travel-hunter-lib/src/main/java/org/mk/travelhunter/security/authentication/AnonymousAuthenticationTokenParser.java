package org.mk.travelhunter.security.authentication;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AnonymousAuthenticationTokenParser implements AuthenticationParser<AnonymousAuthenticationToken> {

	private static final String GUEST = "Guest";

	@Override
	public Class<AnonymousAuthenticationToken> getSupportedAuthenticationType() {
		return AnonymousAuthenticationToken.class;
	}

	@Override
	public String getUserName(Authentication authentication) {
		return GUEST;
	}

	@Override
	public String getAuthenticationProvider(Authentication authentication) {
		return null;
	}

}
