package ru.practicum.shareit.booking;


import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryTest {

    private final TestEntityManager tem;
    private final BookingRepository bookingRepository;

    private final LocalDateTime now = LocalDateTime.now();


    @Test
    void findByIdAndBookerOrOwnerTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("стальной молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.plusDays(3)).end(now.plusDays(5))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        // By Owner
        Booking bookingReturned = bookingRepository
                .findByIdAndBookerOrOwner(booking.getId(), owner.getId()).orElse(null);
        Assertions.assertThat(bookingReturned).isNotNull()
                .isEqualTo(booking);

        // By Owner
        Booking bookingReturned2 = bookingRepository
                .findByIdAndBookerOrOwner(booking.getId(), booker.getId()).orElse(null);
        Assertions.assertThat(bookingReturned2).isNotNull()
                .isEqualTo(booking);


        // Wrong BookingId
        Booking bookingReturned3 = bookingRepository
                .findByIdAndBookerOrOwner(99L, owner.getId()).orElse(null);
        Assertions.assertThat(bookingReturned3).isNull();

        // Wrong BookerId or OwnerId
        Booking bookingReturned4 = bookingRepository
                .findByIdAndBookerOrOwner(booking.getId(), 99L).orElse(null);
        Assertions.assertThat(bookingReturned4).isNull();
    }

    @Test
    void findAllStatusRejectedTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.plusDays(3)).end(now.plusDays(5))
                .status(Status.REJECTED)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByBookerIdAndStatusRejectedOrderByStartDesc(page, booker.getId());
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllStatusWaitingTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.plusDays(3)).end(now.plusDays(5))
                .status(Status.WAITING)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByBookerIdAndStatusWaitingOrderByStartDesc(page, booker.getId());
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllIntoPeriodTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.minusDays(3)).end(now.plusDays(5))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByBookerIdByDateIntoPeriodOrderByStartDesc(page, booker.getId(), now);
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemOwnerTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.minusDays(3)).end(now.plusDays(5))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemOwnerOrderByStartDesc(page, owner.getId());
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemOwnerStartAfterTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.plusDays(3)).end(now.plusDays(5))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemOwnerAndStartIsAfterOrderByStartDesc(page, owner.getId(), now);
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemOwnerAndStateRejectedTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.plusDays(3)).end(now.plusDays(5))
                .status(Status.REJECTED)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemOwnerAndStateRejectedOrderByStartDesc(page, owner.getId());
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemOwnerAndStateWaitingTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.plusDays(3)).end(now.plusDays(5))
                .status(Status.WAITING)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemOwnerAndStateWaitingOrderByStartDesc(page, owner.getId());
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemOwnerByDateIntoPeriodTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.minusDays(3)).end(now.plusDays(5))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemOwnerByDateIntoPeriodOrderByStartDesc(page, owner.getId(), now);
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemOwnerAndEndIsBeforeTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.minusDays(3)).end(now.minusDays(1))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        PageRequest page = PageRequest.of(0, 1);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemOwnerAndEndIsBeforeOrderByStartDesc(page, owner.getId(), now);
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemAndStartIsBeforeTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.minusDays(3)).end(now.minusDays(1))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemIdAndOrderByStartDesc(item.getId(), now);
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemAndStartIsAfterTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.plusDays(3)).end(now.plusDays(5))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemIdOrderByStartAsc(item.getId(), now);
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void findAllByItemUserIdAndItemIdTest() {
        User owner = User.builder().name("owner").email("owner@example.com").build();
        tem.persist(owner);
        Item item = Item.builder()
                .name("молоток").description("крепкий молоток").available(true)
                .owner(owner).build();
        tem.persist(item);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        tem.persist(booker);
        Booking booking = Booking.builder()
                .item(item).booker(booker)
                .start(now.minusDays(5)).end(now.minusDays(3))
                .status(Status.APPROVED)
                .build();
        tem.persist(booking);

        List<Booking> bookingRet = bookingRepository
                .findAllByItemUserIdAndItemIdOrderByStartDesc(booker.getId(), item.getId(), now);
        Assertions.assertThat(bookingRet.get(0)).isNotNull()
                .isEqualTo(booking);
    }
}
