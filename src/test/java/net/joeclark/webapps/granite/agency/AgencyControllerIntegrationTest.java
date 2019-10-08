package net.joeclark.webapps.granite.agency;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers // We'll use testcontainers to spin up a database in Docker to test with.
@ActiveProfiles("test")  // Properties such as spring.datasource.url are loaded from application-test.yml. They'll be overridden, below, but without this line the test will not start.
@ContextConfiguration(initializers = { AgencyControllerIntegrationTest.Initializer.class }) // The child class Initializer dynamically loads DB connection properties as environment variables.
@SpringBootTest
@AutoConfigureMockMvc
class AgencyControllerIntegrationTest {

    @Container
    // spin up a fresh database in a docker container just for the tests below
    private static final GenericContainer dbContainer = new GenericContainer("joeclark77/granite-db:0.1-SNAPSHOT").withExposedPorts(5432);


    // This static child class retrieves information about the temporary database containers so that tests in this class can use them.
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + "jdbc:postgresql://" + dbContainer.getContainerIpAddress() + ":" + dbContainer.getFirstMappedPort() + "/granite",
                    "spring.datasource.username=" + "granite",
                    "spring.datasource.password=" + "test"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void retrievesAgenciesFromDevDatabase() throws Exception {
        this.mockMvc.perform(get("/api/v1/agency"))
                    //.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$..agencyName", Matchers.containsInAnyOrder("Joes Sundries","Clark & Sons"))); // a problematic test because it will only pass if those are the ONLY two agencies in the database; obviously passes only for a carefully specified test DB
    }

}