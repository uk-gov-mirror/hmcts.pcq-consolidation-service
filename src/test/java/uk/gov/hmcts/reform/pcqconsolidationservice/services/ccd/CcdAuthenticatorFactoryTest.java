package uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.pcqconsolidationservice.services.idam.Credential;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CcdAuthenticatorFactoryTest {

    public static final String SERVICE_TOKEN = "SERVICE_TOKEN";
    public static final String JURSIDICTION = "PCQTEST";
    public static final String USER_TOKEN = "123456789";

    private static final String IDAM_USERS_PCQ_USERNAME = "pcq@gmail.com";
    private static final String IDAM_USERS_PCQ_PASSWORD = "password1234";

    private static final String USER_ID = "pcq";

    public static final UserDetails USER_DETAILS = new UserDetails(USER_ID,
            null, null, null, emptyList()
    );

    @Mock
    private AuthTokenGenerator tokenGenerator;

    @Mock
    private IdamClient idamClient;

    @Test
    public void returnSuccessfulCcdAuthenticator() {
        when(idamClient.authenticateUser(any(), any())).thenReturn(USER_TOKEN);
        when(idamClient.getUserDetails(any())).thenReturn(USER_DETAILS);
        when(tokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        CcdAuthenticatorFactory service = new CcdAuthenticatorFactory(tokenGenerator, idamClient);
        CcdAuthenticator authenticator = service.createCcdAuthenticator();

        Assert.assertEquals(SERVICE_TOKEN, authenticator.getServiceToken());
        Assert.assertEquals(USER_TOKEN, authenticator.getUserToken());
        Assert.assertEquals(USER_ID, authenticator.getUserDetails().getId());
    }

    @Test
    public void returnSuccessfulCredentials() {
        Credential user = new Credential(IDAM_USERS_PCQ_USERNAME, IDAM_USERS_PCQ_PASSWORD);

        Assert.assertEquals(IDAM_USERS_PCQ_USERNAME, user.getUsername());
        Assert.assertEquals(IDAM_USERS_PCQ_PASSWORD, user.getPassword());
    }

}
