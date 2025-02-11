package engineering.everest.lhotse.organizations.handlers;

import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.OrganizationAddress;
import engineering.everest.lhotse.organizations.domain.events.*;
import engineering.everest.lhotse.organizations.domain.queries.OrganizationQuery;
import engineering.everest.lhotse.organizations.persistence.Address;
import engineering.everest.lhotse.organizations.persistence.OrganizationsRepository;
import engineering.everest.lhotse.organizations.persistence.PersistableOrganization;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationsEventHandlerTest {

    private static final Instant ORG_CREATION_TIME = Instant.ofEpochSecond(6800L);
    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final String ORGANIZATION_NAME = "organization name";
    private static final String ORGANIZATION_STREET = "street";
    private static final String ORGANIZATION_CITY = "city";
    private static final String ORGANIZATION_STATE = "state";
    private static final String ORGANIZATION_COUNTRY = "country";
    private static final String ORGANIZATION_POSTAL_CODE = "postal";
    private static final String ORGANIZATION_WEBSITE_URL = "website-url";
    private static final String ORGANIZATION_CONTACT_NAME = "contact-name";
    private static final String ORGANIZATION_PHONE_NUMBER = "phone-number";
    private static final String ORGANIZATION_EMAIL_ADDRESS = "email@domain.com";

    private static final OrganizationAddress ORGANIZATION_ADDRESS = new OrganizationAddress(ORGANIZATION_STREET,
        ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE);
    private static final Address ADDRESS = new Address(ORGANIZATION_STREET, ORGANIZATION_CITY,
        ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE);
    private static final String ORGANIZATION_NAME_UPDATE = "organization-name-update";
    private static final String ORGANIZATION_CONTACT_NAME_UPDATE = "contact-name-update";
    private static final String ORGANIZATION_PHONE_NUMBER_UPDATE = "phone-number-update";
    private static final String ORGANIZATION_EMAIL_UPDATE = "emailUpdate@domain.com";
    private static final String ORGANIZATION_STREET_UPDATE = "street-update";
    private static final String ORGANIZATION_CITY_UPDATE = "city-update";
    private static final String ORGANIZATION_STATE_UPDATE = "state-update";
    private static final String ORGANIZATION_POSTAL_CODE_UPDATE = "postal-code-update";
    private static final String ORGANIZATION_WEBSITE_UPDATE = "organization-website-update";

    private OrganizationsEventHandler organizationsEventHandler;

    @Mock
    private QueryUpdateEmitter queryUpdateEmitter;
    @Mock
    private OrganizationsRepository organizationsRepository;

    @BeforeEach
    void setUp() {
        organizationsEventHandler = new OrganizationsEventHandler(queryUpdateEmitter, organizationsRepository);
    }

    @Test
    void prepareForReplay_willDeleteAllProjections() {
        organizationsEventHandler.prepareForReplay();

        verify(organizationsRepository).deleteAll();
    }

    @Test
    void onOrganizationRegisteredByAdminEvent_WillDelegate() {
        organizationsEventHandler.on(new OrganizationCreatedForNewSelfRegisteredUserEvent(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME,
            ORGANIZATION_WEBSITE_URL, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY,
            ORGANIZATION_POSTAL_CODE, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS), ORG_CREATION_TIME);

        verify(organizationsRepository).createOrganization(ORGANIZATION_ID, ORGANIZATION_NAME, ORGANIZATION_ADDRESS,
            ORGANIZATION_WEBSITE_URL,
            ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS, false, ORG_CREATION_TIME);
    }

    @Test
    void onOrganizationEnabledByAdminEvent_WillPersistChanges() {
        var persistableOrganization = mock(PersistableOrganization.class);
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));

        organizationsEventHandler.on(new OrganizationEnabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID));

        verify(persistableOrganization).setDisabled(false);
        verify(organizationsRepository).save(persistableOrganization);
    }

    @Test
    void onOrganizationEnabledByAdminEvent_WillEmitQueryUpdate() {
        var persistableOrganization = mock(PersistableOrganization.class);
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));
        var expectedOrganization = mock(Organization.class);
        when(persistableOrganization.toDomain()).thenReturn(expectedOrganization);

        organizationsEventHandler.on(new OrganizationEnabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID));

        verify(queryUpdateEmitter).emit(eq(OrganizationQuery.class), any(), eq(expectedOrganization));
    }

    @Test
    void onOrganizationDisabledByAdminEvent_WillPersistChanges() {
        var persistableOrganization = mock(PersistableOrganization.class);
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));

        organizationsEventHandler.on(new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID));

        verify(persistableOrganization).setDisabled(true);
        verify(organizationsRepository).save(persistableOrganization);
    }

    @Test
    void onOrganizationDisabledByAdminEvent_WillEmitQueryUpdate() {
        var persistableOrganization = mock(PersistableOrganization.class);
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));
        var expectedOrganization = mock(Organization.class);
        when(persistableOrganization.toDomain()).thenReturn(expectedOrganization);

        organizationsEventHandler.on(new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID));

        verify(queryUpdateEmitter).emit(eq(OrganizationQuery.class), any(), eq(expectedOrganization));
    }

    @Test
    void onOrganizationNameChangedEvent_WillPersistChanges_WhenFieldsHaveChanged() {
        var persistableOrganization = createPersistableOrganization();
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));

        organizationsEventHandler.on(new OrganizationNameChangedEvent(ORGANIZATION_ID, ORGANIZATION_NAME_UPDATE, ADMIN_ID));

        assertEquals(ORGANIZATION_NAME_UPDATE, persistableOrganization.getOrganizationName());
        verify(organizationsRepository).save(persistableOrganization);
    }

    @Test
    void onOrganizationNameChangedEvent_WillEmitQueryUpdate() {
        var persistableOrganization = createPersistableOrganization();
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));
        var expectedOrganization = new Organization(ORGANIZATION_ID, ORGANIZATION_NAME_UPDATE,
            new OrganizationAddress(ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY,
                ORGANIZATION_POSTAL_CODE),
            ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS, false);

        organizationsEventHandler.on(new OrganizationNameChangedEvent(ORGANIZATION_ID, ORGANIZATION_NAME_UPDATE, ADMIN_ID));

        verify(queryUpdateEmitter).emit(eq(OrganizationQuery.class), any(), eq(expectedOrganization));
    }

    @Test
    void onOrganizationContactDetailsUpdatedEvent_WillPersistChanges_WhenFieldsHaveChanged() {
        var persistableOrganization = createPersistableOrganization();
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));

        organizationsEventHandler.on(new OrganizationContactDetailsUpdatedEvent(ORGANIZATION_ID,
            ORGANIZATION_CONTACT_NAME_UPDATE, ORGANIZATION_PHONE_NUMBER_UPDATE, ORGANIZATION_EMAIL_UPDATE, ORGANIZATION_WEBSITE_UPDATE,
            ADMIN_ID));

        assertEquals(ORGANIZATION_CONTACT_NAME_UPDATE, persistableOrganization.getContactName());
        assertEquals(ORGANIZATION_PHONE_NUMBER_UPDATE, persistableOrganization.getPhoneNumber());
        assertEquals(ORGANIZATION_EMAIL_UPDATE, persistableOrganization.getEmailAddress());
        assertEquals(ORGANIZATION_WEBSITE_UPDATE, persistableOrganization.getWebsiteUrl());
        verify(organizationsRepository).save(persistableOrganization);
    }

    @Test
    void onOrganizationContactDetailsUpdatedEvent_WillEmitQueryUpdate() {
        var persistableOrganization = createPersistableOrganization();
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));
        var expectedOrganization = new Organization(ORGANIZATION_ID, ORGANIZATION_NAME,
            new OrganizationAddress(ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY,
                ORGANIZATION_POSTAL_CODE),
            ORGANIZATION_WEBSITE_UPDATE, ORGANIZATION_CONTACT_NAME_UPDATE, ORGANIZATION_PHONE_NUMBER_UPDATE, ORGANIZATION_EMAIL_UPDATE,
            false);

        organizationsEventHandler.on(new OrganizationContactDetailsUpdatedEvent(ORGANIZATION_ID,
            ORGANIZATION_CONTACT_NAME_UPDATE, ORGANIZATION_PHONE_NUMBER_UPDATE, ORGANIZATION_EMAIL_UPDATE, ORGANIZATION_WEBSITE_UPDATE,
            ADMIN_ID));

        verify(queryUpdateEmitter).emit(eq(OrganizationQuery.class), any(), eq(expectedOrganization));
    }

    @Test
    void onOrganizationAddressUpdatedByAdminEvent_WillPersistChanges_WhenFieldsHaveChanged() {
        var persistableOrganization = createPersistableOrganization();
        when(organizationsRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(persistableOrganization));

        organizationsEventHandler.on(new OrganizationAddressUpdatedEvent(ORGANIZATION_ID, ORGANIZATION_STREET_UPDATE,
            ORGANIZATION_CITY_UPDATE, ORGANIZATION_STATE_UPDATE, null, ORGANIZATION_POSTAL_CODE_UPDATE, ADMIN_ID));

        var expectedAddress = new Address(ORGANIZATION_STREET_UPDATE, ORGANIZATION_CITY_UPDATE, ORGANIZATION_STATE_UPDATE,
            ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE_UPDATE);
        assertEquals(expectedAddress, persistableOrganization.getAddress());
        verify(organizationsRepository).save(persistableOrganization);
    }

    private PersistableOrganization createPersistableOrganization() {
        return new PersistableOrganization(ORGANIZATION_ID, ORGANIZATION_NAME, ADDRESS, ORGANIZATION_WEBSITE_URL,
            ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS, false, ORG_CREATION_TIME);
    }
}
