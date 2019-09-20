package net.joeclark.webapps.granite.home;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private GreetingService greetingService;

    /** The homepage for both authenticated and unauthenticated requests, showing different content for each. */
    @GetMapping("/")
    public String publicIndex( Model model ) {
        logger.debug("Processing index page request.");
        model.addAttribute("greeting",greetingService.greeting());
        return "index"; // resolves to src/main/resources/templates/index.html
    }

    /** A dedicated login form for when unauthenticated users attempt to directly access pages other than the homepage */
    @GetMapping("/login")
    public String loginPage() {
        logger.debug("Accessing /login page.");
        return "login";
    }

}
