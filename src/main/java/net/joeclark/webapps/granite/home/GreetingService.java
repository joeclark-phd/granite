package net.joeclark.webapps.granite.home;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    Logger logger = LoggerFactory.getLogger(GreetingService.class);

    public String greeting() {
        logger.trace("Greeting is 'Hello'.");
        return "Hello";
    }

}
