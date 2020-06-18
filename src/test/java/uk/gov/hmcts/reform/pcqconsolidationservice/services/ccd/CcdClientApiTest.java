package uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigHelper;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigItem;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigProvider;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CcdClientApiTest {

    private static final String SERVICE = "pcqtest";
    private static final String CASE_TYPE_ID = "caseTypeA";
    private static final Long CASE_REF = 123_123_123L;
    private static final String PCQ_ID = "1234";
    private static final String ACTOR = "applicant";
    private static final String FIELD = "pcqId";

    public static final String SERVICE_TOKEN = "SERVICE_TOKEN";
    public static final String USER_TOKEN = "USER_token";
    public static final String USER_ID = "USER_ID";

    public static final UserDetails USER_DETAILS = new UserDetails(USER_ID,
            null, null, null, emptyList()
    );
    public static final CcdAuthenticator AUTH_DETAILS = new CcdAuthenticator(
        () -> SERVICE_TOKEN,
            USER_DETAILS,
        () -> USER_TOKEN
    );

    @Mock
    private CoreCaseDataApi feignCcdApi;

    @Mock
    private CcdAuthenticatorFactory authenticatorFactory;

    @Mock
    private ServiceConfigProvider serviceConfigProvider;

    private CcdClientApi testCcdClientApi;

    private ServiceConfigItem service1Config;

    private final CaseDetails caseDetail = CaseDetails.builder().id(CASE_REF).build();

    private final List<CaseDetails> caseDetailsList = Arrays.asList(new CaseDetails[]{caseDetail});

    private final SearchResult searchResult = SearchResult.builder().total(1).cases(caseDetailsList).build();

    @BeforeEach
    public void setUp() {
        service1Config =
                ServiceConfigHelper.serviceConfigItem(
                        SERVICE,
                        singletonList(CASE_TYPE_ID),
                        singletonList(ServiceConfigHelper.createCaseFieldMap(ACTOR, FIELD)));

        testCcdClientApi = new CcdClientApi(feignCcdApi, authenticatorFactory, serviceConfigProvider);
    }

    @Test
    @SuppressWarnings("PMD.DefaultPackage")
    public void useCcdClientToFindCasesByPcqId() {
        when(serviceConfigProvider.getConfig(anyString())).thenReturn(service1Config);
        when(authenticatorFactory.createCcdAuthenticator()).thenReturn(AUTH_DETAILS);
        when(feignCcdApi.searchCases(anyString(), anyString(), anyString(), anyString())).thenReturn(searchResult);

        List<Long> response = testCcdClientApi.getCaseRefsByPcqId(PCQ_ID, SERVICE, ACTOR);
        Assert.assertEquals("Search find correct number of cases", 1, response.size());
        Assert.assertEquals("Search finds correct case", CASE_REF, response.get(0));
    }

}
