package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.lhotse.functionaltests.helpers.TestEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static engineering.everest.lhotse.functionaltests.helpers.TestUtils.assertOk;
import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = DEFINED_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
class ReplayFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private TestEventHandler testEventHandler;
    @Autowired
    private ApiRestTestClient apiRestTestClient;

    @BeforeEach
    void setUp() {
        apiRestTestClient.createAdminUserAndLogin();
    }

    @Test
    void canGetReplayStatus() {
        Map<String, Object> replayStatus = apiRestTestClient.getReplayStatus(OK);
        assertSame(FALSE, replayStatus.get("isReplaying"));
    }

    @Test
    void canTriggerReplayEvents() {
        apiRestTestClient.triggerReplay(NO_CONTENT);
        // This is necessary, otherwise the canGetReplayStatus() may sometimes fail if it runs later
        // and things happen too fast
        assertOk(() -> assertSame(FALSE, apiRestTestClient.getReplayStatus(OK).get("isReplaying")));
    }
}
