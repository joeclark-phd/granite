package net.joeclark.webapps.granite.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

// This filter intercepts the authentication attempt for the REST API expecting a JSON body like {"username":"joe", "password":"pass"}
// and returns a JWT token in the Authorization header.
// Alternatively, I could have just created a RestController and implemented this as an endpoint.  An example of someone who did it that way can be seen
// at: https://github.com/nlpraveennl/springsecurity/blob/master/G_JwtJC/src/main/java/com/gmail/nlpraveennl/controller/RestAPIController.java
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // This Bean is used to deserialize the JSON object sent with a login request (via Jackson ObjectMapper). Maybe a more concise solution is possible?
    static class LoginAttempt {
        private String username;
        private String password;
        public LoginAttempt() {}
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        logger.info("Attempting REST API authentication in JWTAuthenticationFilter");

        LoginAttempt creds;
        try {
            creds = new ObjectMapper().readValue(request.getInputStream(), LoginAttempt.class);
        } catch( IOException e ) {
            // this occurs if JSON body of request is empty or malformed
            throw new org.springframework.security.authentication.AuthenticationServiceException(e.getMessage());
        }

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    creds.getUsername(),
                    creds.getPassword(),
                    new ArrayList<>()
                )
            );

    }

    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth) throws IOException, ServletException {
        String token = Jwts.builder()
                .setSubject(((User) auth.getPrincipal()).getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(SignatureAlgorithm.HS512, "SecretKeyForJWTs")
                .compact();
        response.addHeader("Authorization","Bearer "+token);
    }
}
