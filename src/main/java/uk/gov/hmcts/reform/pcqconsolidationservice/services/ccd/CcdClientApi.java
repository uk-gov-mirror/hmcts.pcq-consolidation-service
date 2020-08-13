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

    public static final String SEARCH_BY_PCQ_ID_QUERY_FORMAT =
            "{\"query\": { \"match_phrase\" : { \"data.%s\" : \"%s\" }}}";

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

        ServiceConfigItem serviceConfig = serviceConfigProvider.getConfig(service);

        if (serviceConfig.getCaseTypeIds().isEmpty()) {
            log.info(
                    "Skipping case search by pcq ID ({}) for service {} because it has no case type ID configured",
                    pcqId,
                    service
            );

            return emptyList();
        } else {
            CcdAuthenticator authenticator = authenticatorFactory.createCcdAuthenticator();
            String caseTypeIdsStr = String.join(",", serviceConfig.getCaseTypeIds());
            String caseFieldNamePcqId = serviceConfig.getCaseField(actor) == null
                    ? SEARCH_BY_PCQ_ID_DEFAULT_FIELD_NAME : serviceConfig.getCaseField(actor);

            log.info(
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

            return searchResult
                    .getCases()
                    .stream()
                    .map(CaseDetails::getId)
                    .collect(toList());
        }
    }
}

