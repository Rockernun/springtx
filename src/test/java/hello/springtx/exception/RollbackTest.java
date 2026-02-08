package hello.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class RollbackTest {

    @Autowired RollbackService rollbackService;

    @Test
    void runtimeException() {
        Assertions.assertThatThrownBy(() -> rollbackService.runtimeException())
                .isInstanceOf(RuntimeException.class);

        /**
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.exception.RollbackTest$RollbackService.runtimeException]
         * h.s.e.RollbackTest$RollbackService       : Call runtimeException...
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.exception.RollbackTest$RollbackService.runtimeException] after exception: java.lang.RuntimeException
         * o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback <-- 롤백됨!!
         * o.s.orm.jpa.JpaTransactionManager        : Rolling back JPA transaction on EntityManager [SessionImpl(159089828<open>)]
         */
    }

    @Test
    void checkedException() {
        Assertions.assertThatThrownBy(() -> rollbackService.checkedException())
                .isInstanceOf(CustomCheckedException.class);

        /**
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.exception.RollbackTest$RollbackService.checkedException]
         * h.s.e.RollbackTest$RollbackService       : Call checkedException...
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.exception.RollbackTest$RollbackService.checkedException] after exception: hello.springtx.exception.RollbackTest$CustomCheckedException
         * o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit <-- 성공적으로 커밋됨
         * o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(424573103<open>)]
         */
    }

    @Test
    void rollbackFor() {
        Assertions.assertThatThrownBy(() -> rollbackService.rollbackFor())
                .isInstanceOf(CustomCheckedException.class);

        /**
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.exception.RollbackTest$RollbackService.rollbackFor]
         * h.s.e.RollbackTest$RollbackService       : Call rollbackFor...
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.exception.RollbackTest$RollbackService.rollbackFor] after exception: hello.springtx.exception.RollbackTest$CustomCheckedException
         * o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback <-- 롤백됨!!
         * o.s.orm.jpa.JpaTransactionManager        : Rolling back JPA transaction on EntityManager [SessionImpl(43951584<open>)]
         */
    }

    @TestConfiguration
    static class RollbackTestConfig {
        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }
    }

    @Slf4j
    static class RollbackService {

        @Transactional
        public void runtimeException() {
            log.info("Call runtimeException...");
            throw new RuntimeException();
        }

        @Transactional
        public void checkedException() throws CustomCheckedException {
            log.info("Call checkedException...");
            throw new CustomCheckedException();
        }

        @Transactional(rollbackFor = CustomCheckedException.class)
        public void rollbackFor() throws CustomCheckedException {
            log.info("Call rollbackFor...");
            throw new CustomCheckedException();
        }
    }

    static class CustomCheckedException extends Exception {}
}
