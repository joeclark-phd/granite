package net.joeclark.webapps.granite.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;


@EnableWebSecurity
public class MultipleSecurityConfigurations {

    Logger logger = LoggerFactory.getLogger(MultipleSecurityConfigurations.class);

    @Configuration
    @Order(1)
    public static class APISecurityConfiguration extends WebSecurityConfigurerAdapter {
        Logger logger = LoggerFactory.getLogger(APISecurityConfiguration.class);

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            JWTAuthenticationFilter jwtAuthenticationFilter = new JWTAuthenticationFilter(authenticationManager());
            jwtAuthenticationFilter.setFilterProcessesUrl("/api/login");

            http.antMatcher("/api/**")
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                    .csrf().disable()
                    .authorizeRequests()
                        .antMatchers(HttpMethod.POST,"/api/login").permitAll()
                        .anyRequest().authenticated()
                        .and()
                    .addFilter(jwtAuthenticationFilter)
                    .addFilter(new JWTAuthorizationFilter(authenticationManager()));

            logger.debug("Set up custom security configuration for Granite API.");
        }

        @Autowired
        private DataSource dataSource;

        @Override
        public void configure(AuthenticationManagerBuilder builder) throws Exception {
            builder .jdbcAuthentication()
                    .dataSource(dataSource);
            logger.debug("Configured app to use JDBC authentication with default database.");
        }

    }

    @Configuration
    public static class FrontEndSecurityConfiguration extends WebSecurityConfigurerAdapter {
        Logger logger = LoggerFactory.getLogger(FrontEndSecurityConfiguration.class);

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
                    .loginPage("/login").permitAll() // Login will often come from the homepage ("/") but if an unauthenticated user tries to directly access another page, they'll see this login page.
                    .defaultSuccessUrl("/")
                    .and()
                    .logout()
                    .logoutUrl("/logout") // I think this is the default, but I like to have it made explicit.
                    .logoutSuccessUrl("/?logout") // Sending "logout" as a query parameter allows us to alert the user he's been logged out.
                    .and()
                    .authorizeRequests()
                    .mvcMatchers("/").permitAll() // This/these endpoint(s) will be viewable by anyone not logged in.
                    .mvcMatchers("/home").authenticated() // All other URL endpoints require authentication or will redirect users to the login page.
                    .anyRequest().denyAll() // Deny-by-default of any unauthorized or malformed URL is a security best practice.
                    .and();
            logger.debug("Set up custom security configuration for Granite front-end.");
        }

        @Autowired
        private DataSource dataSource;

        @Override
        public void configure(AuthenticationManagerBuilder builder) throws Exception {
            builder .jdbcAuthentication()
                    .dataSource(dataSource);
            logger.debug("Configured app to use JDBC authentication with default database.");
        }


    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
