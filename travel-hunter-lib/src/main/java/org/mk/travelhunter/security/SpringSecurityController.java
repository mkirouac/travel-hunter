package org.mk.travelhunter.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mk.travelhunter.security.authentication.AuthenticationParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityController implements SecurityController {

	private static final String GUEST = "Guest";
	private final Map<Class<? extends Authentication>, AuthenticationParser<?>> typeToAuthenticationParserMap;
	
	@Autowired
	public SpringSecurityController(List<AuthenticationParser<?>> authenticationParsers) {
		typeToAuthenticationParserMap = buildTypeToAuthenticationParserMap(authenticationParsers);
	}

	@Override
	public boolean isUserAuthenticated() {
		return !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
	}

	@Override
	public String getUserName() {
		
		if(isUserAuthenticated()) {
			AuthenticationParser<?> parser = getAuthenticationParser();
			return parser.getUserName(getAuthentication());
		} else {
			return GUEST;
		}
	}

	@Override
	public String getAuthenticationProvider() {
		return getAuthenticationParser().getAuthenticationProvider(getAuthentication());
	}
	

	/**
	 * @return the {@link AuthenticationParser} or null if a parser doesn't exists for the specified type.
	 */
	private AuthenticationParser<?> getAuthenticationParser() {
		 AuthenticationParser<?> parser = typeToAuthenticationParserMap.get(getAuthentication().getClass());
		 if(parser == null) {
			 //TODO Externalize
			 parser = new AuthenticationParser<Authentication>() {

				@Override
				public Class<Authentication> getSupportedAuthenticationType() {
					return null;
				}

				@Override
				public String getUserName(Authentication authentication) {
					return GUEST;
				}

				@Override
				public String getAuthenticationProvider(Authentication authentication) {
					return null;
				}
			};
		 }
		 return parser;
	}

	private Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	private Map<Class<? extends Authentication>, AuthenticationParser<?>> buildTypeToAuthenticationParserMap(List<AuthenticationParser<?>> authenticationParsers) {
		Map<Class<? extends Authentication>, AuthenticationParser<?>> map = new HashMap<>();
		for(AuthenticationParser<?> parser : authenticationParsers) {
			map.put(parser.getSupportedAuthenticationType(), parser);
		}
		return map;
	}

}
