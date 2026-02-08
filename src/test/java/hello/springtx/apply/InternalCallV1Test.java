package hello.springtx.apply;

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
public class InternalCallV1Test {

    @Autowired CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
        /**
         * h.springtx.apply.InternalCallV1Test      : callService class=class hello.springtx.apply.InternalCallV1Test$CallService$$SpringCGLIB$$0
         */
    }

    @Test
    void internalMethodCall() {
        callService.internal();
        /**
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.apply.InternalCallV1Test$CallService.internal]
         * h.s.a.InternalCallV1Test$CallService     : Call internal...
         * h.s.a.InternalCallV1Test$CallService     : Transaction Active: true
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.apply.InternalCallV1Test$CallService.internal]
         */
    }

    @Test
    void externalMethodCall() {
        callService.external();
        /**
         * h.s.a.InternalCallV1Test$CallService     : Call external...
         * h.s.a.InternalCallV1Test$CallService     : Transaction Active: false
         * h.s.a.InternalCallV1Test$CallService     : Call internal...
         * h.s.a.InternalCallV1Test$CallService     : Transaction Active: false <--트랜잭션 적용 안 됨(문제 발생!!!)
         */
    }

    @TestConfiguration
    static class InternalCallV1Config {
        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class CallService {

        public void external() {
            log.info("Call external...");
            printTxInfo();
            internal();
        }

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
