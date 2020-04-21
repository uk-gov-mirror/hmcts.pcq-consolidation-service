package uk.gov.hmcts.reform.pcqconsolidationservice;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "uk.gov.hmcts.reform")
@Slf4j
public class ConsolidationApplication implements ApplicationRunner {

    @Autowired
    private ConsolidationComponent consolidationComponent;

    @Autowired
    private TelemetryClient client;

    @Value("${telemetry.wait.period:10000}")
    private int waitPeriod;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        try {
            consolidationComponent.execute();
        } catch (Exception e) {
            log.error("Error executing Consolidation service", e);
            throw e;
        } finally {
            client.flush();
            waitTelemetryGracefulPeriod();
        }

    }

    private void waitTelemetryGracefulPeriod() throws InterruptedException {
        Thread.sleep(waitPeriod);
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsolidationApplication.class);
    }
}