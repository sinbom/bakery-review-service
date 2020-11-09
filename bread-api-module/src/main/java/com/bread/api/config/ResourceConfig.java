package com.bread.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class ResourceConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources
                .resourceId("");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .mvcMatchers("/api/**")
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated();
        http
                .sessionManagement()
                .sessionCreationPolicy(STATELESS);
        http
                .csrf()
                .disable();
        http
                .formLogin()
                .disable();
        http
                .logout()
                .disable();
        http
                .exceptionHandling()
                .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }

}
