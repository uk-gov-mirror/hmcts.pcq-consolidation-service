package uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd;

import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class CcdAuthenticator {

    private final UserDetails userDetails;
    private final Supplier<String> serviceTokenSupplier;
    private final Supplier<String> userTokenSupplier;
    private final LocalDateTime userTokenCreationDate;

    public CcdAuthenticator(
            Supplier<String> serviceTokenSupplier,
            UserDetails userDetails,
            Supplier<String> userTokenSupplier
    ) {
        this.serviceTokenSupplier = serviceTokenSupplier;
        this.userDetails = userDetails;
        this.userTokenSupplier = userTokenSupplier;
        this.userTokenCreationDate = LocalDateTime.now();
    }

    public String getUserToken() {
        return this.userTokenSupplier.get();
    }

    public String getServiceToken() {
        return this.serviceTokenSupplier.get();
    }

    public UserDetails getUserDetails() {
        return this.userDetails;
    }

    public long userTokenAgeInSeconds() {
        LocalDateTime now = LocalDateTime.now();
        Duration dur = Duration.between(now, userTokenCreationDate);
        return Math.abs(dur.toSeconds());
    }
}
