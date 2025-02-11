package engineering.everest.lhotse.organizations.services;

import engineering.everest.lhotse.organizations.domain.commands.DisableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.EnableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.UpdateOrganizationCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultOrganizationsServiceTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final String ORG_ADMIN_EMAIL_ADDRESS_1 = "EmailAddress";
    private static final String ORGANIZATION_NAME = "organization-name";
    private static final String ORGANIZATION_STREET_1 = "street-1";
    private static final String ORGANIZATION_CITY_1 = "city-1";
    private static final String ORGANIZATION_STATE_1 = "state-1";
    private static final String ORGANIZATION_COUNTRY_1 = "country-1";
    private static final String ORGANIZATION_POSTAL_CODE_1 = "postal-1";
    private static final String ORGANIZATION_WEBSITE_URL_1 = "website-1";
    private static final String ORGANIZATION_CONTACT_NAME_1 = "contactName";
    private static final String ORGANIZATION_PHONE_NUMBER_1 = "phoneNumber";

    @Mock
    private CommandGateway commandGateway;

    private DefaultOrganizationsService defaultOrganizationService;

    @BeforeEach
    void setUp() {
        defaultOrganizationService = new DefaultOrganizationsService(commandGateway);
    }

    @Test
    void updateOrganisation_WillSendCommandAndWaitForCompletion() {
        defaultOrganizationService.updateOrganization(ADMIN_ID, ORGANIZATION_ID, ORGANIZATION_NAME,
            ORGANIZATION_STREET_1, ORGANIZATION_CITY_1, ORGANIZATION_STATE_1,
            ORGANIZATION_COUNTRY_1, ORGANIZATION_POSTAL_CODE_1, ORGANIZATION_WEBSITE_URL_1, ORGANIZATION_CONTACT_NAME_1,
            ORGANIZATION_PHONE_NUMBER_1, ORG_ADMIN_EMAIL_ADDRESS_1);

        verify(commandGateway).sendAndWait(new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME,
            ORGANIZATION_STREET_1, ORGANIZATION_CITY_1, ORGANIZATION_STATE_1,
            ORGANIZATION_COUNTRY_1, ORGANIZATION_POSTAL_CODE_1, ORGANIZATION_WEBSITE_URL_1, ORGANIZATION_CONTACT_NAME_1,
            ORGANIZATION_PHONE_NUMBER_1, ORG_ADMIN_EMAIL_ADDRESS_1));
    }

    @Test
    void enableOrganisation_WillSendCommandAndWaitForCompletion() {
        defaultOrganizationService.enableOrganization(ADMIN_ID, ORGANIZATION_ID);
        verify(commandGateway).sendAndWait(new EnableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID));
    }

    @Test
    void disableOrganisation_WillSendCommandAndWaitForCompletion() {
        defaultOrganizationService.disableOrganization(ADMIN_ID, ORGANIZATION_ID);
        verify(commandGateway).sendAndWait(new DisableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID));
    }
}
