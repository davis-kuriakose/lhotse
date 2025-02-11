package engineering.everest.lhotse.organizations.domain;

import engineering.everest.lhotse.organizations.domain.events.OrganizationAddressUpdatedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationContactDetailsUpdatedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationCreatedForNewSelfRegisteredUserEvent;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@NoArgsConstructor
class OrganizationContactDetails implements Serializable {

    @EntityId
    private UUID organizationId;
    private String websiteUrl;
    private String contactName;
    private String contactPhoneNumber;
    private String contactEmail;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    @EventSourcingHandler
    void on(OrganizationCreatedForNewSelfRegisteredUserEvent event) {
        contactName = event.getContactName();
        contactEmail = event.getContactEmail();
        contactPhoneNumber = event.getContactPhoneNumber();
        websiteUrl = event.getWebsiteUrl();
    }

    @EventSourcingHandler
    void on(OrganizationContactDetailsUpdatedEvent event) {
        contactName = event.getContactName();
        contactEmail = event.getEmailAddress();
        contactPhoneNumber = event.getPhoneNumber();
        websiteUrl = event.getWebsiteUrl();
    }

    @EventSourcingHandler
    void on(OrganizationAddressUpdatedEvent event) {
        city = event.getCity();
        country = event.getCountry();
        postalCode = event.getPostalCode();
        state = event.getState();
        street = event.getStreet();
    }
}
