package org.mk.travelhunter.security.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationTokenParser implements AuthenticationParser<OAuth2AuthenticationToken> {

	@Override
	public Class<OAuth2AuthenticationToken> getSupportedAuthenticationType() {
		return OAuth2AuthenticationToken.class;
	}

	@Override
	public String getUserName(Authentication authentication) {
		
		//This doesn't seem to be reliable... Probably better with a meaningless userid.
		
		OAuth2AuthenticationToken token = cast(authentication);
		OAuth2User user = token.getPrincipal();
		
		//FIXME
		String userName = user.getAttribute("login");//github
		if(userName == null) {
			userName = user.getAttribute("email");//google
		}
		return userName;//email
	}

	@Override
	public String getAuthenticationProvider(Authentication authentication) {
		
		OAuth2AuthenticationToken token = cast(authentication);
		return token.getAuthorizedClientRegistrationId();
	}

	
	private OAuth2AuthenticationToken cast(Authentication authentication) {
		
		if(!OAuth2AuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
			throw new IllegalArgumentException("The Authentication of type " + authentication.getClass() + " is not assignable from OAuth2AuthenticationToken");
		}
		return (OAuth2AuthenticationToken)authentication;
	}
	
}
