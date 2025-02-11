package engineering.everest.lhotse.organizations.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validators.EmailAddressValidator;
import engineering.everest.lhotse.axon.command.validators.UsersBelongToOrganizationValidator;
import engineering.everest.lhotse.organizations.domain.commands.CreateSelfRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.DisableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.EnableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.UpdateOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.events.OrganizationAddressUpdatedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationContactDetailsUpdatedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationCreatedForNewSelfRegisteredUserEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationDisabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationEnabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationNameChangedEvent;
import engineering.everest.lhotse.organizations.domain.events.UserPromotedToOrganizationAdminEvent;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolationException;
import java.util.UUID;

import static engineering.everest.lhotse.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrganizationAggregateTest {

    private static final UUID ORGANIZATION_ID = UUID.fromString("3e1d8663-6bb1-45e6-8d4a-8dce9f95fe2a");
    private static final UUID REGISTERING_USER_ID = UUID.fromString("bd4c08da-8ce7-4ef4-af57-cc407c3cb848");
    private static final String ORGANIZATION_NAME = "organization-name";
    private static final String NO_CHANGE = null;
    private static final String MISSING_ARGUMENT = null;
    private static final String ORGANIZATION_STREET = "street";
    private static final String ORGANIZATION_CITY = "city";
    private static final String ORGANIZATION_STATE = "state";
    private static final String ORGANIZATION_COUNTRY = "country";
    private static final String ORGANIZATION_POSTAL_CODE = "postal";
    private static final String ORGANIZATION_WEBSITE_URL = "website-url";
    private static final String ORGANIZATION_CONTACT_NAME = "contact-name";
    private static final String ORGANIZATION_CONTACT_PHONE_NUMBER = "phone-number";
    private static final String ORGANIZATION_CONTACT_EMAIL_ADDRESS = "email@domain.com";
    private static final OrganizationCreatedForNewSelfRegisteredUserEvent ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT =
        new OrganizationCreatedForNewSelfRegisteredUserEvent(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME, ORGANIZATION_WEBSITE_URL,
            ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
            ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

    private static final OrganizationDisabledByAdminEvent ORGANIZATION_DISABLED_BY_ADMIN_EVENT =
        new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID);

    private FixtureConfiguration<OrganizationAggregate> testFixture;
    private AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @Mock
    private EmailAddressValidator emailAddressValidator;
    @Mock
    private UsersBelongToOrganizationValidator usersBelongToOrganizationValidator;

    @BeforeEach
    void setUp() {
        axonCommandExecutionExceptionFactory = new AxonCommandExecutionExceptionFactory();

        testFixture = new AggregateTestFixture<>(OrganizationAggregate.class)
            .registerCommandHandlerInterceptor(mockCommandValidatingMessageHandlerInterceptor(
                emailAddressValidator, usersBelongToOrganizationValidator))
            .registerInjectableResource(axonCommandExecutionExceptionFactory);
    }

    @Test
    void aggregateHasExplicitlyDefinedSnapshotTrigger() {
        var organizationClass = OrganizationAggregate.class;
        var aggregateAnnotation = organizationClass.getAnnotation(Aggregate.class);
        assertEquals("organizationAggregateSnapshotTriggerDefinition", aggregateAnnotation.snapshotTriggerDefinition());
    }

    @Test
    void emits_WhenCreateSelfRegisteredOrganizationCommandIsValid() {
        testFixture.givenNoPriorActivity()
            .when(new CreateSelfRegisteredOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME, ORGANIZATION_STREET,
                ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS))
            .expectEvents(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT);
    }

    @Test
    void organizationsCreatedForNewSelfRegisteredUsersAreEnabled() {
        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(new EnableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("ORGANIZATION_ALREADY_ENABLED");
    }

    @Test
    void rejectsCreateRegisteredOrganizationCommand_WhenRequestingSelfRegisteredUserIdIsNull() {
        var command = new CreateSelfRegisteredOrganizationCommand(ORGANIZATION_ID, null, ORGANIZATION_NAME, ORGANIZATION_STREET,
            ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
            ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.givenNoPriorActivity()
            .when(command)
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateRegisteredOrganizationCommand_WhenSelfRegisteredOrganizationNameIsBlank() {
        testFixture.givenNoPriorActivity()
            .when(new CreateSelfRegisteredOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, NO_CHANGE, ORGANIZATION_STREET,
                ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS))
            .expectException(ConstraintViolationException.class)
            .expectNoEvents();
    }

    @Test
    void rejectsCreateRegisteredOrganizationCommand_WhenSelfRegisteredOrganizationNameIsNull() {
        testFixture.givenNoPriorActivity()
            .when(new CreateSelfRegisteredOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, MISSING_ARGUMENT, ORGANIZATION_STREET,
                ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS))
            .expectException(ConstraintViolationException.class)
            .expectNoEvents();
    }

    @Test
    void emits_WhenDisableSelfRegisteredOrganizationCommandIsValid() {
        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(new DisableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
            .expectEvents(new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID));
    }

    @Test
    void emits_WhenSelfRegisteredEnableOrganizationCommandIsValid() {
        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT,
            new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID))
            .when(new EnableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
            .expectEvents(new OrganizationEnabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID));
    }

    @Test
    void rejectsEnableOrganizationCommand_WhenSelfRegisteredOrganizationIsAlreadyEnabled() {
        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(new EnableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("ORGANIZATION_ALREADY_ENABLED");
    }

    @Test
    void rejectsDisableOrganizationCommand_WhenSelfRegisteredOrganizationIsAlreadyDisabled() {
        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT, ORGANIZATION_DISABLED_BY_ADMIN_EVENT)
            .when(new DisableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("ORGANIZATION_IS_DISABLED");
    }

    @Test
    void rejectsDisableOrganizationCommand_WhenSelfRegisteredRequestingUserIdIsNull() {
        var command = new DisableOrganizationCommand(ORGANIZATION_ID, null);

        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(ConstraintViolationException.class)
            .expectExceptionMessage("requestingUserId: must not be null");
    }

    @Test
    void updateOrganizationCommandEmitsSingleEvent_WhenOnlySelfRegisteredUserOrganizationNameChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, "new org name",
            NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE);

        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(command)
            .expectEvents(new OrganizationNameChangedEvent(ORGANIZATION_ID, "new org name", ADMIN_ID));
    }

    @Test
    void updateOrganizationCommandEmitsSingleEvent_WhenOnlySelfRegisteredUserOrganizationContactDetailsChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, NO_CHANGE, NO_CHANGE, NO_CHANGE,
            NO_CHANGE, NO_CHANGE, NO_CHANGE, ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER,
            ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(command)
            .expectEvents(
                new OrganizationContactDetailsUpdatedEvent(ORGANIZATION_ID, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER,
                    ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_WEBSITE_URL, ADMIN_ID));
    }

    @Test
    void updateOrganizationCommandEmitsSingleEvent_WhenOnlySelfRegisteredUserOrganizationAddressChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, NO_CHANGE, ORGANIZATION_STREET, ORGANIZATION_CITY,
            ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE);

        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(command)
            .expectEvents(
                new OrganizationAddressUpdatedEvent(ORGANIZATION_ID, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                    ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, ADMIN_ID));
    }

    @Test
    void updateOrganizationCommandEmitsMultipleEvents_WhenSelfRegisteredUserOrganiztionNameChangedAndContactDetailsChangedAndAddressChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, "new org name",
            ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
            ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
            ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(command)
            .expectEvents(
                new OrganizationNameChangedEvent(ORGANIZATION_ID, "new org name", ADMIN_ID),
                new OrganizationContactDetailsUpdatedEvent(ORGANIZATION_ID, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER,
                    ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_WEBSITE_URL, ADMIN_ID),
                new OrganizationAddressUpdatedEvent(ORGANIZATION_ID, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                    ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, ADMIN_ID));
    }

    @Test
    void rejectsUpdateOrganizationCommand_WhenSelfRegisteredRequestingUserIdIsNull() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, null, ORGANIZATION_NAME,
            ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
            ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
            ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(ConstraintViolationException.class)
            .expectExceptionMessage("requestingUserId: must not be null");
    }

    @Test
    void rejectsUpdateOrganizationCommand_WhenSelfRegisteredOrganizationIsAlreadyDisabled() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME,
            ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
            ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
            ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT, ORGANIZATION_DISABLED_BY_ADMIN_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("ORGANIZATION_IS_DISABLED");
    }

    @Test
    void rejectsUpdateOrganizationCommand_WhenSelfRegisteredUserNoFieldsAreBeingChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, NO_CHANGE, NO_CHANGE,
            NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE);

        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("ORGANIZATION_UPDATE_NO_FIELDS_CHANGED");
    }

    @Test
    void emits_WhenPromoteUserToOrganizationAdminCommandIsValid() {
        testFixture.given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT)
            .when(new PromoteUserToOrganizationAdminCommand(ORGANIZATION_ID, REGISTERING_USER_ID))
            .expectEvents(new UserPromotedToOrganizationAdminEvent(ORGANIZATION_ID, REGISTERING_USER_ID));
    }

    @Test
    void rejectsPromoteUserToOrganizationAdminCommand_WhenOrganizationIsDisabled() {
        testFixture
            .given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT, new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID))
            .when(new PromoteUserToOrganizationAdminCommand(ORGANIZATION_ID, REGISTERING_USER_ID))
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("ORGANIZATION_IS_DISABLED");
    }

    @Test
    void rejectsPromoteUserToOrganizationAdminCommand_WhenUserIsAlreadyAnAdmin() {
        testFixture
            .given(ORGANIZATION_CREATED_FOR_NEW_SELF_REGISTERED_USER_EVENT,
                new UserPromotedToOrganizationAdminEvent(ORGANIZATION_ID, REGISTERING_USER_ID))
            .when(new PromoteUserToOrganizationAdminCommand(ORGANIZATION_ID, REGISTERING_USER_ID))
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("USER_ALREADY_ORGANIZATION_ADMIN");
    }
}
