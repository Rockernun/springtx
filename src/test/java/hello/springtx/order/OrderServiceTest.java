package hello.springtx.order;

import static org.assertj.core.api.Fail.fail;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    void complete() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUserName("정상");

        orderService.order(order);

        Order findOrder = orderRepository.findById(order.getId()).get();
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("결제 완료");

        /**
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
         * hello.springtx.order.OrderService        : 결제 프로세스 시작
         * hello.springtx.order.OrderService        : 정상 승인
         * hello.springtx.order.OrderService        : 결제 프로세스 종료
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.order.OrderService.order]
         * o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
         */
    }

    @Test
    void runtimeException() {
        Order order = new Order();
        order.setUserName("예외");

        Assertions.assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(RuntimeException.class);

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        Assertions.assertThat(orderOptional.isEmpty()).isTrue();

        /**
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
         * hello.springtx.order.OrderService        : 결제 프로세스 시작
         * hello.springtx.order.OrderService        : 시스템 예외 발생
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.order.OrderService.order] after exception: java.lang.RuntimeException: 시스템 예외
         * o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback
         */
    }

    @Test
    void notEnoughMoneyException() {
        Order order = new Order();
        order.setUserName("잔고 부족");

        try {
            orderService.order(order);
            fail("잔고 부족 예외가 발생해야 합니다.");
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족을 알리고 별도 계좌로 입금하도록 안내한다.");
        }

        Order findOrder = orderRepository.findById(order.getId()).get();
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("대기");

        /**
         * o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
         * hello.springtx.order.OrderService        : 결제 프로세스 시작
         * hello.springtx.order.OrderService        : 잔고 부족 비즈니스 예외 발생
         * o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.order.OrderService.order] after exception: hello.springtx.order.NotEnoughMoneyException: 잔고가 부족합니다.
         * o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
         * o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(126778243<open>)]
         * o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(126778243<open>)] after transaction
         * hello.springtx.order.OrderServiceTest    : 고객에게 잔고 부족을 알리고 별도 계좌로 입금하도록 안내한다.
         */
    }
}
