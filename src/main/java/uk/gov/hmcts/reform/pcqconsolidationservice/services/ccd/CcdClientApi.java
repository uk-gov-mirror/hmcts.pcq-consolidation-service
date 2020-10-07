package uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigItem;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigProvider;

import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class CcdClientApi {

    private final CoreCaseDataApi feignCcdApi;
    private final ServiceConfigProvider serviceConfigProvider;
    private final CcdAuthenticatorFactory authenticatorFactory;
    private CcdAuthenticator authenticator;

    public static final long USER_TOKEN_REFRESH_IN_SECONDS = 300;

    public static final String SEARCH_BY_PCQ_ID_QUERY_FORMAT =
            "{\"query\": { \"match_phrase\" : { \"data.%s\" : \"%s\" }}}";

    public static final String SEARCH_BY_DCN_QUERY_FORMAT =
            "{\"query\": { \"match_phrase\" : { \"data.scannedDocuments.value.controlNumber\" : \"%s\" }}}";

    public static final String SEARCH_BY_PCQ_ID_DEFAULT_FIELD_NAME = "pcqId";

    public CcdClientApi(
            CoreCaseDataApi feignCcdApi,
            CcdAuthenticatorFactory authenticator,
            ServiceConfigProvider serviceConfigProvider
    ) {
        this.feignCcdApi = feignCcdApi;
        this.authenticatorFactory = authenticator;
        this.serviceConfigProvider = serviceConfigProvider;
    }

    public List<Long> getCaseRefsByPcqId(String pcqId, String service, String actor) {

        refreshExpiredIdamToken();
        ServiceConfigItem serviceConfig = serviceConfigProvider.getConfig(service);

        if (serviceConfig.getCaseTypeIds().isEmpty()) {
            log.info(
                    "Skipping case search by pcq ID ({}) for service {} because it has no case type ID configured",
                    pcqId,
                    service
            );
            return emptyList();

        } else {
            String caseTypeIdsStr = String.join(",", serviceConfig.getCaseTypeIds());
            String caseFieldNamePcqId = serviceConfig.getCaseField(actor) == null
                    ? SEARCH_BY_PCQ_ID_DEFAULT_FIELD_NAME : serviceConfig.getCaseField(actor);

            log.debug(
                    "Searching for pcqId {} within the service {} using ES query {}",
                    pcqId,
                    service,
                    format(SEARCH_BY_PCQ_ID_QUERY_FORMAT, caseFieldNamePcqId, pcqId)
            );
            SearchResult searchResult = feignCcdApi.searchCases(
                    authenticator.getUserToken(),
                    authenticator.getServiceToken(),
                    caseTypeIdsStr,
                    format(SEARCH_BY_PCQ_ID_QUERY_FORMAT, caseFieldNamePcqId, pcqId)
            );
            return searchResult.getCases().stream().map(CaseDetails::getId).collect(toList());
        }
    }

    public List<Long> getCaseRefsByOriginatingFormDcn(String dcn, String service) {

        refreshExpiredIdamToken();
        ServiceConfigItem serviceConfig = serviceConfigProvider.getConfig(service);

        if (serviceConfig.getCaseTypeIds().isEmpty()) {
            log.debug(
                    "Skipping case search by DCN ({}) for service {} because it has no case type ID configured",
                    dcn,
                    service
            );
            return emptyList();

        } else {
            String caseTypeIdsStr = String.join(",", serviceConfig.getCaseTypeIds());

            log.debug(
                    "Searching for DCN {} within the service {} using ES query {}",
                    dcn,
                    service,
                    format(SEARCH_BY_DCN_QUERY_FORMAT, dcn)
            );
            SearchResult searchResult = feignCcdApi.searchCases(
                    authenticator.getUserToken(),
                    authenticator.getServiceToken(),
                    caseTypeIdsStr,
                    format(SEARCH_BY_DCN_QUERY_FORMAT, dcn)
            );
            return searchResult.getCases().stream().map(CaseDetails::getId).collect(toList());
        }
    }

    private void refreshExpiredIdamToken() {
        if (this.authenticator == null
                || this.authenticator.userTokenAgeInSeconds() > USER_TOKEN_REFRESH_IN_SECONDS) {
            log.info("Refeshing user token.");
            this.authenticator = authenticatorFactory.createCcdAuthenticator();
        }
    }
}

