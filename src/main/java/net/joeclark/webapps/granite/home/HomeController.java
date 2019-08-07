package net.joeclark.webapps.granite.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private GreetingService greetingService;

    /** The publicly-visible homepage, in this case featuring a login form. */
    @GetMapping("/")
    public String publicIndex( @RequestParam(name="name",required=false,defaultValue="World") String name, Model model ) {
        model.addAttribute("greeting",greetingService.greeting());
        model.addAttribute("name",name);
        return "index"; // resolves to src/main/resources/templates/index.html
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

}
