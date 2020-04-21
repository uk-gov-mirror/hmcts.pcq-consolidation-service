package uk.gov.hmcts.reform.pcqconsolidationservice.service;

import org.springframework.http.ResponseEntity;

public interface PcqBackendService {

    ResponseEntity getPcqWithoutCase();

}
