package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
@Slf4j
public class TxLevelTest {

    @Autowired LevelService levelService;

    @Test
    void orderTest() {
        levelService.write();
        levelService.read();
        /**
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.apply.TxLevelTest$LevelService.write]
         * hello.springtx.apply.TxLevelTest         : call write
         * hello.springtx.apply.TxLevelTest         : Transaction Active: true
         * hello.springtx.apply.TxLevelTest         : Transaction ReadOnly: false
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.apply.TxLevelTest$LevelService.write]
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.apply.TxLevelTest$LevelService.read]
         * hello.springtx.apply.TxLevelTest         : call read
         * hello.springtx.apply.TxLevelTest         : Transaction Active: true
         * hello.springtx.apply.TxLevelTest         : Transaction ReadOnly: true
         */
    }

    @TestConfiguration
    static class TxApplyLevelConfig {
        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }

    @Transactional(readOnly = true)
    static class LevelService {

        @Transactional(readOnly = false)
        public void write() {
            log.info("call write");
            printTxInfo();
        }

        public void read() {
            log.info("call read");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Transaction Active: {}", txActive);
            boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("Transaction ReadOnly: {}", isReadOnly);
        }
    }
}
