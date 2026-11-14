package com.ashlin.clinic.config;

import com.ashlin.clinic.web.AuthGuardFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class WebConfig {

	public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

	@Bean
	public Clock clock() {
		return Clock.system(ZONE);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public ViewResolver jspViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/jsp/");
		resolver.setSuffix(".jsp");
		resolver.setOrder(1);
		return resolver;
	}

	@Bean
	public FilterRegistrationBean<AuthGuardFilter> authGuard() {
		FilterRegistrationBean<AuthGuardFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new AuthGuardFilter());
		registration.addUrlPatterns("/*");
		registration.setOrder(1);
		return registration;
	}
}
