package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired BasicService basicService;

    @Test
    void proxyCheck() {
        log.info("AOP class: {}", basicService.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(basicService)).isTrue();
        /**
         * 2026-02-07T14:42:34.719+09:00  INFO 41344 --- [springtx] [    Test worker] hello.springtx.apply.TxBasicTest
         * : AOP class: class hello.springtx.apply.TxBasicTest$BasicService$$SpringCGLIB$$0
         */
    }

    @Test
    void txTest() {
        basicService.tx();
        basicService.nonTx();
        /**
         * 2026-02-07T14:41:34.735+09:00  INFO 40055 --- [springtx] [    Test worker] h.s.apply.TxBasicTest$BasicService       : Call Transaction...
         * 2026-02-07T14:41:34.735+09:00  INFO 40055 --- [springtx] [    Test worker] h.s.apply.TxBasicTest$BasicService       : Transaction Active: true
         * 2026-02-07T14:41:34.737+09:00  INFO 40055 --- [springtx] [    Test worker] h.s.apply.TxBasicTest$BasicService       : Call Non-Transaction...
         * 2026-02-07T14:41:34.737+09:00  INFO 40055 --- [springtx] [    Test worker] h.s.apply.TxBasicTest$BasicService       : Transaction Active: false
         */
    }

    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        BasicService basicService() {
            return new BasicService();
        }
    }

    @Slf4j
    static class BasicService {

        @Transactional
        public void tx() {
            log.info("Call Transaction...");
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Transaction Active: {}", txActive);
        }

        public void nonTx() {
            log.info("Call Non-Transaction...");
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Transaction Active: {}", txActive);
        }
    }
}
