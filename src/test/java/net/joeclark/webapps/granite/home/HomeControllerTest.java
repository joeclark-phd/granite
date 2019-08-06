package net.joeclark.webapps.granite.home;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GreetingService service;

    @Test
    public void greetingShouldReturnMessageFromService() throws Exception {
        when(service.greeting()).thenReturn("Bonjour");
        mockMvc.perform(get("/"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Bonjour World")));
    }

    @Test
    public void homePageShouldBeBlockedToUnauthorizedGuests() throws Exception {
        mockMvc.perform(get("/home"))
            .andDo(print())
            .andExpect(status().is3xxRedirection());
    }

    @WithMockUser(value="user")
    @Test
    public void homePageShouldBeAccessibleToAuthorizedUser() throws Exception {
        mockMvc.perform(get("/home"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Your Home Page")));
    }

}