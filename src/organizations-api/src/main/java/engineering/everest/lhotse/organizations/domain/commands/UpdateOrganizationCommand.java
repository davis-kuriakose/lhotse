package engineering.everest.lhotse.organizations.domain.commands;

import engineering.everest.lhotse.axon.command.validation.OrganizationStatusValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationCommand implements OrganizationStatusValidatableCommand {

    @TargetAggregateIdentifier
    private UUID organizationId;
    @NotNull
    private UUID requestingUserId;
    private String organizationName;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String websiteUrl;
    private String contactName;
    private String phoneNumber;
    private String emailAddress;

}
