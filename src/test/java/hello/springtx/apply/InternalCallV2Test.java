package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired CallService callService;

    @Test
    void externalMethodCallV2() {
        callService.external();
        /**
         * h.s.a.InternalCallV2Test$CallService     : Call external...
         * h.s.a.InternalCallV2Test$CallService     : Transaction Active: false <-- CallService에서는 트랜잭션 적용 X
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.apply.InternalCallV2Test$InternalService.internal]
         * h.s.a.InternalCallV2Test$InternalService : Call internal...
         * h.s.a.InternalCallV2Test$InternalService : Transaction Active: true <-- InternalService에서 트랜잭션 정상적으로 적용
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.apply.InternalCallV2Test$InternalService.internal]
         */
    }

    @TestConfiguration
    static class InternalCallV2Config {
        @Bean
        CallService callService() {
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("Call external...");
            printTxInfo();
            internalService.internal();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Transaction Active: {}", txActive);
        }
    }

    @Slf4j
    static class InternalService {

        @Transactional
        public void internal() {
            log.info("Call internal...");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Transaction Active: {}", txActive);
        }
    }
}
