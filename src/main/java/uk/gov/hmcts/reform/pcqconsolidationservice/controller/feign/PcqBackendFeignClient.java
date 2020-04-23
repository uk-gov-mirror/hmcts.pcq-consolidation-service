package uk.gov.hmcts.reform.pcqconsolidationservice.controller.feign;

import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.FeignInterceptorConfiguration;

@FeignClient(name = "PcqBackendFeignClient", url = "${pcqBackendUrl:http://localhost:4550}", configuration =
        FeignInterceptorConfiguration.class)
public interface PcqBackendFeignClient {

    @GetMapping("/pcq/backend/consolidation/pcqWithoutCase")
    @RequestLine("GET /pcq/backend/consolidation/pcqWithoutCase")
    Response getPcqWithoutCase(@RequestHeader("X-Correlation-Id") String token);

}
