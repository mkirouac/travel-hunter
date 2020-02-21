package org.mk.travelhunter.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfiguration  extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()//Seems to cause problems with Vaadin, should find a real solution
				.authorizeRequests()
					.anyRequest().permitAll()//.authenticated()
			.and()
				.oauth2Login();
	}

}