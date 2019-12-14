package com.matjuillard.useridentapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.matjuillard.useridentapp.service.UserService;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

	@Value("${login-url}")
	private String loginUrl;
	@Value("${email-verification-url}")
	private String emailVerificationUrl;
	@Value("${password-reset-request-url}")
	private String passwordResetRequestUrl;
	@Value("${password-reset-url}")
	private String passwordResetUrl;

	private final UserService userDetailsService;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public WebSecurity(UserService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.userDetailsService = userDetailsService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.headers().frameOptions().sameOrigin();
		http.csrf().disable();
		// add authentication filter
		http.authorizeRequests().and().addFilter(getAuthenticationFilter());
		// add authorization filter
		http.authorizeRequests().and().addFilter(new AuthorizationFilter(authenticationManager()));

		// Create a user auth - no authentication needed
		http.authorizeRequests().antMatchers(HttpMethod.POST, loginUrl).permitAll();
		// Email reset Password url - no authentication needed
		http.authorizeRequests().antMatchers(HttpMethod.GET, emailVerificationUrl).permitAll();
		// Email reset password url - no authentication needed
		http.authorizeRequests().antMatchers(HttpMethod.POST, passwordResetRequestUrl).permitAll();
		http.authorizeRequests().antMatchers(HttpMethod.POST, passwordResetUrl).permitAll();
		// H2 console access
		http.authorizeRequests().antMatchers("/h2-console/**").permitAll();
		// Other requests - needs to be authenticated
		http.authorizeRequests().anyRequest().authenticated();
		// Stateless session
		http.authorizeRequests().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	// Change default URL to login from /login to users/login
	public AuthenticationFilter getAuthenticationFilter() throws Exception {
		final AuthenticationFilter filter = new AuthenticationFilter(authenticationManager());
		filter.setFilterProcessesUrl("/users/login");
		return filter;
	}

}
