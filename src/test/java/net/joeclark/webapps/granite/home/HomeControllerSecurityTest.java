package net.joeclark.webapps.granite.home;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
class HomeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private GreetingService service;


    @Test
    @DisplayName("The index page / should be publicly accessible.")
    public void indexPageShouldBePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Images in the /public folder should be publicly accessible.")
    public void publicFolderShouldBePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/public/granite3.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    @DisplayName("The /images folder should be blocked to those not logged in.")
    public void imagesFolderShouldBeBlockedToUnauthorizedGuests() throws Exception {
        // The /images folder is public in the default Spring Boot Security configuration, so this test makes
        // sure we've successfully overridden that.  Use the /public folder for anything that should be public.
        mockMvc.perform(get("/images/granite.jpg"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("The /css folder should be blocked to those not logged in.")
    public void cssFolderShouldBeBlockedToUnauthorizedGuests() throws Exception {
        // The /css folder is public in the default Spring Boot Security configuration, so this test makes
        // sure we've successfully overridden that.  Use the /public folder for anything that should be public.
        mockMvc.perform(get("/css/bootstrap.css"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("The index page / should show certain contanet ONLY to those not logged in.")
    public void indexPageShowsCertainContentOnlyToThoseNotLoggedIn() throws Exception {
        mockMvc.perform(get("/"))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign In")))
                .andExpect(content().string(not(containsString("You are authenticated."))));
    }

    @WithMockUser(value="user")
    @Test
    @DisplayName("The index page / should show certain content ONLY to logged-in users.")
    public void indexPageShowsCertainContentOnlyToLoggedInUsers() throws Exception {
        mockMvc.perform(get("/"))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You are authenticated.")))
                .andExpect(content().string(not(containsString("Sign In"))));
    }



}