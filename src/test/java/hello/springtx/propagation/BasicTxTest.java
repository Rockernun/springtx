package hello.springtx.propagation;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired PlatformTransactionManager transactionManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("Transaction Start...");
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("Trying to Transaction Commit...");
        transactionManager.commit(status);
        log.info("Transaction successfully Committed!");
    }

    @Test
    void rollback() {
        log.info("Transaction Start...");
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("Trying to Transaction Rollback...");
        transactionManager.rollback(status);
        log.info("Transaction successfully Rollback!");
    }
}
