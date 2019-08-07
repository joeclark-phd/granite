package net.joeclark.webapps.granite;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/").permitAll() // Homepage (only) will be viewable by anyone not logged in.
                .antMatchers("/favicon.ico").permitAll() // Spring Boot looks for a favicon in src/main/resources and points this URL at it. Delete this line to use the default (leaf) favicon.
                .antMatchers("/webjars/**").permitAll() // This directory is where Maven downloads bootstrap, jquery, etc.
                .antMatchers("/public/**").permitAll() // A directory for images + CSS. I hope that naming it 'public' cautions future developers against over-using it.
                .anyRequest().authenticated() // All other URL endpoints require authentication or will redirect users to the login page.
            .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
            .and()
            .logout()
                .logoutSuccessUrl("/");
    }

}
