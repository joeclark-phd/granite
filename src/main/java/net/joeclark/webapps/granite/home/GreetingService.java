package net.joeclark.webapps.granite.home;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String greeting() {
        return "Hello";
    }

}
