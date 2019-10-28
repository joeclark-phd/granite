package net.joeclark.webapps.granite.home;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
class HomeControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private GreetingService service;

    @WithMockUser(value="joe")
    @Test
    @DisplayName("The homepage should use a GreetingService to retrieve a greeting message.")
    public void greetingShouldReturnMessageFromService() throws Exception {
        when(service.greeting()).thenReturn("Bonjour");
        mockMvc.perform(get("/"))
            //.andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Bonjour")));
        verify(service,times(1)).greeting(); // verifies that service.greeting() was called once
    }

}