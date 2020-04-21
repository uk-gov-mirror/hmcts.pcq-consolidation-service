package uk.gov.hmcts.reform.pcqconsolidationservice.controller.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.FeignInterceptorConfiguration;

@FeignClient(name = "PcqBackendFeignClient", url = "${pcqBackendUrl}", configuration = FeignInterceptorConfiguration.class)
public interface PcqBackendFeignClient {

    @GetMapping(value = "/pcq/backend/consolidation/pcqWithoutCase")
    @RequestLine("GET /pcq/backend/consolidation/pcqWithoutCase")
    @Headers({"X-Correlation-Id: ${coRelationId}", "Content-Type: application/json"})
    Response getPcqWithoutCase();

}
