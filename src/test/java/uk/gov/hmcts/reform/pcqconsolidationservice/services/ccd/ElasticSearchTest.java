package uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ElasticSearchTest {

    private static final String JURISDICTION = "PCQ";
    private static final String SERVICE = "pcqtest";
    private static final String CASE_TYPE_ID = "caseTypeA";
    private static final Long CASE_REF = 123123123L;
    private static final String PCQ_ID = "1234";

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

    @Mock
    private CcdAuthenticator authenticator;

    private CcdClientApi elasticSearch;

    private ServiceConfigItem service1Config;

    CaseDetails caseDetail = CaseDetails.builder().id(CASE_REF).build();

    List<CaseDetails> caseDetailsList = Arrays.asList(new CaseDetails[]{caseDetail});

    private SearchResult searchResult = SearchResult.builder().total(1).cases(caseDetailsList).build();

    @BeforeEach
    public void setUp() {
        service1Config =
                ServiceConfigHelper.serviceConfigItem(SERVICE, JURISDICTION, Arrays.asList(CASE_TYPE_ID));

        elasticSearch = new CcdClientApi(feignCcdApi, authenticatorFactory, serviceConfigProvider);
    }

    @Test
    public void useCcdClientToFindCasesByPcqId() {
        when(serviceConfigProvider.getConfig(anyString())).thenReturn(service1Config);
        when(authenticatorFactory.createForJurisdiction(anyString())).thenReturn(AUTH_DETAILS);
        when(feignCcdApi.searchCases(anyString(), anyString(), anyString(), anyString())).thenReturn(searchResult);

        List<Long> response = elasticSearch.getCaseRefsByPcqId(PCQ_ID, SERVICE);
        Assert.assertEquals("Search find correct number of cases", 1, response.size());
        Assert.assertEquals("Search finds correct case", CASE_REF, response.get(0));
    }

}
