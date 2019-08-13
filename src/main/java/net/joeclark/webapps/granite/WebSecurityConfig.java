package net.joeclark.webapps.granite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers(
                "/favicon.ico",  // Spring Boot looks for a favicon in src/main/resources and points this URL at it. Delete this line to use the default (leaf) favicon.
                 "/webjars/**", // This directory is where Maven downloads bootstrap, jquery, etc.
                "/public/**" // A directory for static images + CSS. I hope that naming it 'public' cautions future developers against over-using it.
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .formLogin()
                .loginPage("/login").permitAll()
                .and()
            .logout()
                .logoutSuccessUrl("/")
                .and()
            .authorizeRequests()
                .mvcMatchers("/").permitAll() // This/these endpoint(s) will be viewable by anyone not logged in.
                .mvcMatchers("/home").authenticated() // All other URL endpoints require authentication or will redirect users to the login page.
                .anyRequest().denyAll(); // Deny-by-default of any unauthorized or malformed URL is a security best practice.
        logger.debug("Executed custom security configuration for Granite project.");
    }


}
