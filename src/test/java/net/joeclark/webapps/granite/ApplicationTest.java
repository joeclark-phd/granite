package net.joeclark.webapps.granite;

import net.joeclark.webapps.granite.home.HomeController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationIT {

    @Autowired
    private HomeController homeController;

    @Test
    @DisplayName("Application context loads.")
    public void contextLoads() throws Exception {
        assertNotNull(homeController);
    }



}