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

    @Test
    void double_commit() {
        log.info("Transaction1 Start...");
        TransactionStatus transaction1 = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("Transaction1 Committed!");
        transactionManager.commit(transaction1);

        log.info("Transaction2 Start...");
        TransactionStatus transaction2 = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("Transaction2 Committed!");
        transactionManager.commit(transaction2);

        /**
         * hello.springtx.propagation.BasicTxTest   : Transaction1 Start...
         * o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
         * o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@1585654158 wrapping conn0: url=jdbc:h2:mem:0caf0047-403b-4f2c-bb88-f5a5a10f0c9a user=SA] for JDBC transaction
         * o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@1585654158 wrapping conn0: url=jdbc:h2:mem:0caf0047-403b-4f2c-bb88-f5a5a10f0c9a user=SA] to manual commit
         * hello.springtx.propagation.BasicTxTest   : Transaction1 Committed!
         * o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
         * o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@1585654158 wrapping conn0: url=jdbc:h2:mem:0caf0047-403b-4f2c-bb88-f5a5a10f0c9a user=SA]
         * o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@1585654158 wrapping conn0: url=jdbc:h2:mem:0caf0047-403b-4f2c-bb88-f5a5a10f0c9a user=SA] after transaction
         *
         * hello.springtx.propagation.BasicTxTest   : Transaction2 Start...
         * o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
         * o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@110041993 wrapping conn0: url=jdbc:h2:mem:0caf0047-403b-4f2c-bb88-f5a5a10f0c9a user=SA] for JDBC transaction
         * o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@110041993 wrapping conn0: url=jdbc:h2:mem:0caf0047-403b-4f2c-bb88-f5a5a10f0c9a user=SA] to manual commit
         * hello.springtx.propagation.BasicTxTest   : Transaction2 Committed!
         * o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
         * o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@110041993 wrapping conn0: url=jdbc:h2:mem:0caf0047-403b-4f2c-bb88-f5a5a10f0c9a user=SA]
         * o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@110041993 wrapping conn0: url=jdbc:h2:mem:0caf0047-403b-4f2c-bb88-f5a5a10f0c9a user=SA] after transaction
         */
    }

    @Test
    void double_commit_rollback() {
        log.info("Transaction1 Start...");
        TransactionStatus transaction1 = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("Transaction1 Committed!");
        transactionManager.commit(transaction1);

        log.info("Transaction2 Start...");
        TransactionStatus transaction2 = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("Transaction2 Rollback!");
        transactionManager.rollback(transaction2);

        /**
         * o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@2130422201 wrapping conn0: url=jdbc:h2:mem:b675ecf7-4c8a-4e8b-bf66-e11b39fc89b2 user=SA] for JDBC transaction
         * o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@2130422201 wrapping conn0: url=jdbc:h2:mem:b675ecf7-4c8a-4e8b-bf66-e11b39fc89b2 user=SA] to manual commit
         * hello.springtx.propagation.BasicTxTest   : Transaction1 Committed!
         *
         * o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@643565394 wrapping conn0: url=jdbc:h2:mem:b675ecf7-4c8a-4e8b-bf66-e11b39fc89b2 user=SA] for JDBC transaction
         * o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@643565394 wrapping conn0: url=jdbc:h2:mem:b675ecf7-4c8a-4e8b-bf66-e11b39fc89b2 user=SA] to manual commit
         * hello.springtx.propagation.BasicTxTest   : Transaction2 Rollback!
         */
    }

    @Test
    void inner_commit() {
        log.info("Outer Transaction Start...");
        TransactionStatus outerTx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outerTx.isNewTransaction()={}", outerTx.isNewTransaction());

        log.info("Inner Transaction Start...");
        TransactionStatus innerTx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("innerTx.isNewTransaction()={}", innerTx.isNewTransaction());
        log.info("Inner Transaction Committed!");
        transactionManager.commit(innerTx);

        log.info("Outer Transaction Committed!");
        transactionManager.commit(outerTx);

        /**
         * hello.springtx.propagation.BasicTxTest   : outerTx.isNewTransaction()=true
         * hello.springtx.propagation.BasicTxTest   : Inner Transaction Start...
         * o.s.j.d.DataSourceTransactionManager     : Participating in existing transaction
         * hello.springtx.propagation.BasicTxTest   : innerTx.isNewTransaction()=false
         * hello.springtx.propagation.BasicTxTest   : Inner Transaction Committed!
         * hello.springtx.propagation.BasicTxTest   : Outer Transaction Committed!
         */
    }
}
