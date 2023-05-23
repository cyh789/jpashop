package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Book;
import jpabook.jpashop.domain.Item;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RunWith(SpringRunner.class)
class OrderServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    private Member member;
    private Item item;

    @BeforeEach
    void init() {
        member = createMember();
        item = createBook();
    }

    @Test
    void 상품주문() throws Exception {
        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        Order getOrder = orderRepository.findOne(orderId);
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(getOrder.getOrderItems().size()).isEqualTo(1);
        assertThat(getOrder.getTotalPrice()).isEqualTo(10000 * 2);
        assertThat(item.getStockQuantity()).isEqualTo(8);
    }

    private Member createMember() {
        member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }

    private Book createBook() {
        Book book = new Book();
        book.setName("시골 JPA");
        book.setStockQuantity(10);
        book.setPrice(10000);
        em.persist(book);
        return book;
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        int orderCount = 11; //재고보다 많은 수량

        assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), item.getId(), orderCount);
        });
    }

    @Test
    public void 주문취소() throws Exception {
        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(),orderCount);

        orderService.cancelOrder(orderId);

        Order getOrder = orderRepository.findOne(orderId);
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(item.getStockQuantity()).isEqualTo(10);
    }

    @Test
    public void 주문검색_상태확인_승인() throws Exception {
        int orderCount = 2;

        //승인
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setOrderStatus(OrderStatus.ORDER);
        List<Order> getOrders = orderRepository.findAllByOrderStatus(orderSearch);
        assertThat(getOrders.size()).isEqualTo(1);

        orderSearch.setOrderStatus(OrderStatus.CANCEL);
        getOrders = orderRepository.findAllByOrderStatus(orderSearch);
        assertThat(getOrders.size()).isEqualTo(0);
    }

    @Test
    public void 주문검색_상태확인_취소() throws Exception {
        int orderCount = 2;

        //승인
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);
        //취소
        orderService.cancelOrder(orderId);

        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setOrderStatus(OrderStatus.ORDER);
        List<Order>  getOrders = orderRepository.findAllByOrderStatus(orderSearch);
        assertThat(getOrders.size()).isEqualTo(0);

        orderSearch.setOrderStatus(OrderStatus.CANCEL);
        getOrders = orderRepository.findAllByOrderStatus(orderSearch);
        assertThat(getOrders.size()).isEqualTo(1);
    }
}