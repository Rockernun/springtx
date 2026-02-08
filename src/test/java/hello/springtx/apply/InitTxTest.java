package hello.springtx.apply;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class InitTxTest {

    @Autowired TransactionAppliedAtInit txAppliedAtInit;

    @Test
    void run() {
        /**
         * .s.a.InitTxTest$TransactionAppliedAtInit : Is Transaction Active when PostConstruct? false <-- 트랜잭션 적용 X
         * hello.springtx.apply.InitTxTest          : Started InitTxTest in 0.992 seconds (process running for 1.35) <-- 그 다음 스프링 컨테이너가 생성
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.apply.InitTxTest$TransactionAppliedAtInit.initV2]
         * 2.s.a.InitTxTest$TransactionAppliedAtInit : Is Transaction Active when ApplicationReadyEvent? true <-- 트랜잭션 적용 O
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.apply.InitTxTest$TransactionAppliedAtInit.initV2]
         */
    }

    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        TransactionAppliedAtInit transactionAppliedAtInit() {
            return new TransactionAppliedAtInit();
        }
    }

    @Slf4j
    static class TransactionAppliedAtInit {

        @PostConstruct
        @Transactional
        public void initV1() {
            boolean txIsActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Is Transaction Active when PostConstruct? {}", txIsActive);
        }

        @EventListener(value = ApplicationReadyEvent.class)
        @Transactional
        public void initV2() {
            boolean txIsActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Is Transaction Active when ApplicationReadyEvent? {}", txIsActive);
        }
    }
}
