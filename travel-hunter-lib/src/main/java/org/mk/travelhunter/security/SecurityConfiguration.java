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
//				
//				// Vaadin Flow static resources
//			    // Now, those requests are handled by Spring Security's filter chain which results in a fully initialized
//			    // security context. This is used in the upload's success listener to do additional authentication checks for example.
//			    .antMatchers("/VAADIN/**").permitAll() // 
//
//			    // Allow all requests by logged in users.
//			    .anyRequest().authenticated()

				
			.and()
				.oauth2Login();
	}

}