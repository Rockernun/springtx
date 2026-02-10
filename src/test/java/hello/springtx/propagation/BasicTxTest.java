package hello.springtx.propagation;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
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

    @Test
    void outer_rollback() {
        log.info("Outer Transaction Start...");
        TransactionStatus outerTx = transactionManager.getTransaction(new DefaultTransactionDefinition());

        log.info("Inner Transaction Start...");
        TransactionStatus innerTx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("Inner Transaction Committed!");
        transactionManager.commit(innerTx);

        log.info("Outer Transaction Rollback!");
        transactionManager.rollback(outerTx);

        /**
         * hello.springtx.propagation.BasicTxTest   : Inner Transaction Start...
         * o.s.j.d.DataSourceTransactionManager     : Participating in existing transaction
         * hello.springtx.propagation.BasicTxTest   : Inner Transaction Committed!
         * hello.springtx.propagation.BasicTxTest   : Outer Transaction Rollback!
         * o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
         * o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@635569126 wrapping conn0: url=jdbc:h2:mem:0743c509-623e-4709-a182-e77f4c08c1d3 user=SA]
         */
    }

    @Test
    void inner_rollback() {
        log.info("Outer Transaction Start...");
        TransactionStatus outerTx = transactionManager.getTransaction(new DefaultTransactionDefinition());

        log.info("Inner Transaction Start...");
        TransactionStatus innerTx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("Inner Transaction Rollback!");
        transactionManager.rollback(innerTx);

        log.info("Outer Transaction Committed!");
        Assertions.assertThatThrownBy(() -> transactionManager.commit(outerTx))
                .isInstanceOf(UnexpectedRollbackException.class);

        /**
         * org.springframework.transaction.UnexpectedRollbackException: Transaction rolled back because it has been marked as rollback-only
         *
         * 	at org.springframework.transaction.support.AbstractPlatformTransactionManager.processRollback(AbstractPlatformTransactionManager.java:938)
         * 	at org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:754)
         * 	at hello.springtx.propagation.BasicTxTest.inner_rollback(BasicTxTest.java:162)
         *
         * 	<상세 로그>
         * 	hello.springtx.propagation.BasicTxTest   : Inner Transaction Start...
         * o.s.j.d.DataSourceTransactionManager     : Participating in existing transaction
         * hello.springtx.propagation.BasicTxTest   : Inner Transaction Rollback!
         * o.s.j.d.DataSourceTransactionManager     : Participating transaction failed - marking existing transaction as rollback-only <-- 참여 중인 트랜잭션(내부 트랜잭션) 실패, 외부 트랜잭션을 rollback-only로 마킹
         * o.s.j.d.DataSourceTransactionManager     : Setting JDBC transaction [HikariProxyConnection@219665748 wrapping conn0: url=jdbc:h2:mem:02ab0ac9-d4a9-46f7-8722-cfe4812927d0 user=SA] rollback-only
         * hello.springtx.propagation.BasicTxTest   : Outer Transaction Committed!
         * o.s.j.d.DataSourceTransactionManager     : Global transaction is marked as rollback-only but transactional code requested commit <-- rollback-only로 설정했는데 외부 트랜잭션 커밋 시도(불가능)
         * o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
         * o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@219665748 wrapping conn0: url=jdbc:h2:mem:02ab0ac9-d4a9-46f7-8722-cfe4812927d0 user=SA]
         */
    }

    @Test
    void inner_rollback_requires_now() {
        log.info("Outer Transaction Start...");
        TransactionStatus outerTx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outerTx.isNewTransaction()={}", outerTx.isNewTransaction());

        log.info("Inner Transaction Start...");
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus innerTx = transactionManager.getTransaction(definition);
        log.info("innerTx.isNewTransaction()={}", innerTx.isNewTransaction());

        log.info("Inner Transaction Rollback!");
        transactionManager.rollback(innerTx);

        log.info("Outer Transaction Committed!");
        transactionManager.commit(outerTx);

        /**
         * hello.springtx.propagation.BasicTxTest   : Outer Transaction Start...
         * o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
         * o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@521961438 wrapping conn0: url=jdbc:h2:mem:15e1fb0c-47cb-4d61-bd8d-383c48ffcc0d user=SA] for JDBC transaction
         * o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@521961438 wrapping conn0: url=jdbc:h2:mem:15e1fb0c-47cb-4d61-bd8d-383c48ffcc0d user=SA] to manual commit
         * hello.springtx.propagation.BasicTxTest   : outerTx.isNewTransaction()=true
         * hello.springtx.propagation.BasicTxTest   : Inner Transaction Start...
         * o.s.j.d.DataSourceTransactionManager     : Suspending current transaction, creating new transaction with name [null] <-- 기존 외부 트랜잭션은 잠시 미뤄두고 새로운 트랜잭션 생성
         * o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@504760990 wrapping conn1: url=jdbc:h2:mem:15e1fb0c-47cb-4d61-bd8d-383c48ffcc0d user=SA] for JDBC transaction
         * o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@504760990 wrapping conn1: url=jdbc:h2:mem:15e1fb0c-47cb-4d61-bd8d-383c48ffcc0d user=SA] to manual commit
         * hello.springtx.propagation.BasicTxTest   : innerTx.isNewTransaction()=true
         * hello.springtx.propagation.BasicTxTest   : Inner Transaction Rollback! <-- 내부 트랜잭션은 롤백 처리
         * o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
         * o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@504760990 wrapping conn1: url=jdbc:h2:mem:15e1fb0c-47cb-4d61-bd8d-383c48ffcc0d user=SA]
         * o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@504760990 wrapping conn1: url=jdbc:h2:mem:15e1fb0c-47cb-4d61-bd8d-383c48ffcc0d user=SA] after transaction
         * o.s.j.d.DataSourceTransactionManager     : Resuming suspended transaction after completion of inner transaction <-- 잠시 미뤄뒀던 기존 외부 트랜잭션 계속 작업
         * hello.springtx.propagation.BasicTxTest   : Outer Transaction Committed! <-- 외부 트랜잭션은 커밋 처리
         * o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
         * o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@521961438 wrapping conn0: url=jdbc:h2:mem:15e1fb0c-47cb-4d61-bd8d-383c48ffcc0d user=SA]
         * o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@521961438 wrapping conn0: url=jdbc:h2:mem:15e1fb0c-47cb-4d61-bd8d-383c48ffcc0d user=SA] after transaction
         */
    }
}
