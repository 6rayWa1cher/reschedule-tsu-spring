package com.a6raywa1cher.rescheduletsuspring.security;

import com.a6raywa1cher.rescheduletsuspring.dao.repository.LessonCellRepository;
import com.a6raywa1cher.rescheduletsuspring.dao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private UserRepository userRepository;
	private LessonCellRepository lessonCellRepository;

	@Autowired
	public SecurityConfig(UserRepository userRepository, LessonCellRepository lessonCellRepository) {
		this.userRepository = userRepository;
		this.lessonCellRepository = lessonCellRepository;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(new UsernamePasswordAuthenticationProvider(userDetailsService(), passwordEncoder()));
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
			.antMatchers("/").permitAll()
			.antMatchers("/user/**").hasRole("USER")
			.antMatchers("/user/reg", "/user/delete_user").hasRole("ADMIN")
			.antMatchers("/cells/**").hasRole("USER")
			.antMatchers("/cells/force", "/cells/delete_sudo").hasRole("ADMIN")
			.antMatchers("/v2/api-docs", "/webjars/**", "/swagger-resources", "/swagger-resources/**",
				"/swagger-ui.html").permitAll()
			.antMatchers("/csrf").permitAll()
			.anyRequest().permitAll()
			.and()
			.httpBasic()
			.and()
			.cors()
			.configurationSource(corsConfigurationSource());
	}

	@Bean
	public UsernamePasswordAuthenticationProvider authenticationManager(PasswordEncoder passwordEncoder) {
		return new UsernamePasswordAuthenticationProvider(userDetailsService(), passwordEncoder);
	}

	@Override
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl(userRepository, lessonCellRepository);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PATCH", "PUT", "HEAD", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}