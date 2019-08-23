package net.joeclark.webapps.granite.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GraniteUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username.equals("joe")) {
            return User.withDefaultPasswordEncoder()
                    .username("joe")
                    .password("test")
                    .roles("test")
                    .build();
        } else {
            return null;
        }
    }
}
