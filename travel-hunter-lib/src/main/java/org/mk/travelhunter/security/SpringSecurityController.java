package org.mk.travelhunter.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mk.travelhunter.security.authentication.AuthenticationParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SpringSecurityController implements SecurityController {

	static final String GUEST = "Guest";
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
		AuthenticationParser<?> parser = null;
		Authentication authentication = getAuthentication();
		if(authentication != null) {
			parser = typeToAuthenticationParserMap.get(getAuthentication().getClass());
		} else {
			log.warn("No Authentication was found. This probably indicates that the SecurityContextHolder was not populated with the SecurityContext");
		}
		 if(parser == null) {
			 parser = new NullAuthenticationParser();
		 }
		 return parser;
	}

	private Authentication getAuthentication() {
		SecurityContext context = SecurityContextHolder.getContext();
		return context == null ? null : context.getAuthentication();
	}
	
	private Map<Class<? extends Authentication>, AuthenticationParser<?>> buildTypeToAuthenticationParserMap(List<AuthenticationParser<?>> authenticationParsers) {
		Map<Class<? extends Authentication>, AuthenticationParser<?>> map = new HashMap<>();
		for(AuthenticationParser<?> parser : authenticationParsers) {
			map.put(parser.getSupportedAuthenticationType(), parser);
		}
		return map;
	}

}
