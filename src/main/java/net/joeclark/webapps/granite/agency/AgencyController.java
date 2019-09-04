package net.joeclark.webapps.granite.agency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AgencyController {

    @Autowired
    AgencyService agencyService;

    @GetMapping("/api/v1/agency")
    public List<Agency> listAgencies() {
        return agencyService.findAll();
    }

}
