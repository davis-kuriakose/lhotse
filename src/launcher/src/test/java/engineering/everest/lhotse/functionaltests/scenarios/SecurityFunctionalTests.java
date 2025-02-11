package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
class SecurityFunctionalTests {

    @Autowired
    private WebTestClient webClient;

    @Test
    @WithAnonymousUser
    void applicationIsAbleToStart() {
        webClient.get().uri("/api/version")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class);
    }

    @Test
    @EnabledIfSystemProperty(named = "org.gradle.project.buildDir", matches = ".+")
    @WithAnonymousUser
    void swaggerApiDocIsAccessible() throws IOException {
        String apiContent = webClient.get().uri("/api/doc")
            .exchange()
            .expectStatus().isOk()
            .returnResult(String.class).getResponseBody().blockFirst();

        assertNotNull(apiContent);

        Files.writeString(
            Paths.get(System.getProperty("org.gradle.project.buildDir"), "web-app-api.json"),
            apiContent);
    }

    @Test
    void retrievingOrganizationListWillRedirectToLogin_WhenUserIsNotAuthenticated() {
        webClient.get().uri("/admin/organizations")
            .exchange()
            .expectStatus().isFound();
    }
}
