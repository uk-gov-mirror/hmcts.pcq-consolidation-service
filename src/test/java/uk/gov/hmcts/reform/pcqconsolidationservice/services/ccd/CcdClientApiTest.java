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

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd.CcdClientApi.SEARCH_BY_PCQ_ID_QUERY_FORMAT;

@ExtendWith(MockitoExtension.class)
public class CcdClientApiTest {

    private static final String SERVICE = "pcqtest";
    private static final String CASE_TYPE_ID = "caseTypeA";
    private static final Long CASE_REF = 123_123_123L;
    private static final String PCQ_ID = "1234";
    private static final String ACTOR = "applicant";
    private static final String DEFAULT_PCQID_FIELD = "pcqId";
    private static final String APPLICANT_PCQID_FIELD = "applicantPcdId";

    public static final String SERVICE_TOKEN = "SERVICE_TOKEN";
    public static final String USER_TOKEN = "USER_TOKEN";
    public static final String USER_ID = "USER_ID";
    public static final String SEARCH_CASES_DEFAULT_PCQ_FIELD_SEARCH_STRING
            = format(SEARCH_BY_PCQ_ID_QUERY_FORMAT, DEFAULT_PCQID_FIELD, PCQ_ID);

    public static final String SEARCH_CASES_APPLICANT_PCQ_FIELD_SEARCH_STRING
            = format(SEARCH_BY_PCQ_ID_QUERY_FORMAT, APPLICANT_PCQID_FIELD, PCQ_ID);

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

    private ServiceConfigItem serviceConfigWithCustomCcdFieldMapping;

    private ServiceConfigItem serviceConfigWithMissingIncorrectActorCcdFieldMapping;

    private final CaseDetails caseDetail = CaseDetails.builder().id(CASE_REF).build();

    private final List<CaseDetails> caseDetailsList = Arrays.asList(new CaseDetails[]{caseDetail});

    private final SearchResult searchResult = SearchResult.builder().total(1).cases(caseDetailsList).build();

    @BeforeEach
    public void setUp() {
        serviceConfigWithCustomCcdFieldMapping =
                ServiceConfigHelper.serviceConfigItem(
                        SERVICE,
                        singletonList(CASE_TYPE_ID),
                        singletonList(ServiceConfigHelper.createCaseFieldMap(ACTOR, APPLICANT_PCQID_FIELD)));

        serviceConfigWithMissingIncorrectActorCcdFieldMapping =
                ServiceConfigHelper.serviceConfigItem(
                        SERVICE,
                        singletonList(CASE_TYPE_ID),
                        null);

        testCcdClientApi = new CcdClientApi(feignCcdApi, authenticatorFactory, serviceConfigProvider);
    }

    @Test
    @SuppressWarnings("PMD.DefaultPackage")
    public void useCcdClientToFindCasesByPcqIdWithNoPcqFieldMapping() {
        when(serviceConfigProvider.getConfig(anyString()))
                .thenReturn(serviceConfigWithMissingIncorrectActorCcdFieldMapping);
        when(authenticatorFactory.createCcdAuthenticator()).thenReturn(AUTH_DETAILS);
        when(feignCcdApi.searchCases(
                eq(USER_TOKEN),
                eq(SERVICE_TOKEN),
                eq(CASE_TYPE_ID),
                eq(SEARCH_CASES_DEFAULT_PCQ_FIELD_SEARCH_STRING))).thenReturn(searchResult);

        List<Long> response = testCcdClientApi.getCaseRefsByPcqId(PCQ_ID, SERVICE, ACTOR);
        Assert.assertEquals("Search find correct number of cases", 1, response.size());
        Assert.assertEquals("Search find correct case", CASE_REF, response.get(0));
    }

    @Test
    @SuppressWarnings("PMD.DefaultPackage")
    public void useCcdClientToFindCasesByPcqIdWithCustomPcqField() {
        when(serviceConfigProvider.getConfig(anyString())).thenReturn(serviceConfigWithCustomCcdFieldMapping);
        when(authenticatorFactory.createCcdAuthenticator()).thenReturn(AUTH_DETAILS);
        when(feignCcdApi.searchCases(
                eq(USER_TOKEN),
                eq(SERVICE_TOKEN),
                eq(CASE_TYPE_ID),
                eq(SEARCH_CASES_APPLICANT_PCQ_FIELD_SEARCH_STRING))).thenReturn(searchResult);

        List<Long> response = testCcdClientApi.getCaseRefsByPcqId(PCQ_ID, SERVICE, ACTOR);
        Assert.assertEquals("Search find correct number of cases", 1, response.size());
        Assert.assertEquals("Search find correct case", CASE_REF, response.get(0));
    }

}
