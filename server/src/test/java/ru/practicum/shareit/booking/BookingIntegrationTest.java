package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class BookingIntegrationTest {

    private final EntityManager em;
    private final BookingService bookingService;

    @Test
    void test_getAllBookingsForOwner() {
        LocalDateTime now = LocalDateTime.now();
        User owner = User.builder().name("owner").email("owner@example.com").build();
        em.persist(owner);
        User booker = User.builder().name("booker").email("booker@example.com").build();
        em.persist(booker);
        Item item1 = Item.builder()
                .name("молоток").description("хороший молоток").available(true)
                .owner(owner).build();
        em.persist(item1);
        Item item2 = Item.builder()
                .name("дрель").description("мощная дрель").available(true)
                .owner(owner).build();
        em.persist(item2);
        // current approved
        Booking booking1 = Booking.builder()
                .item(item1).booker(booker).status(Status.APPROVED)
                .start(now.minusDays(1)).end(now.plusDays(1))
                .build();
        em.persist(booking1);
        // future waiting
        Booking booking2 = Booking.builder()
                .item(item1).booker(booker).status(Status.WAITING)
                .start(now.plusDays(1)).end(now.plusDays(2))
                .build();
        em.persist(booking2);
        // past
        Booking booking3 = Booking.builder()
                .item(item2).booker(booker).status(Status.REJECTED)
                .start(now.minusDays(2)).end(now.minusDays(1))
                .build();
        em.persist(booking3);
        Booking booking4 = Booking.builder()
                .item(item2).booker(booker).status(Status.CANCELED)
                .start(now.minusDays(1)).end(now.plusDays(1))
                .build();
        em.persist(booking4);


        List<BookingOutDto> list1 = bookingService.findAllBookingByUserAndState(booker.getId(), "ALL", 0, 20);
        Assertions.assertThat(list1).isNotEmpty().hasSize(4);

        List<BookingOutDto> list2 = bookingService.findAllBookingByUserAndState(booker.getId(), "PAST", 0, 20);
        Assertions.assertThat(list2).isNotEmpty().hasSize(1);

        List<BookingOutDto> list3 = bookingService.findAllBookingByUserAndState(booker.getId(), "FUTURE", 0, 20);
        Assertions.assertThat(list3).isNotEmpty().hasSize(1);

        List<BookingOutDto> list4 = bookingService.findAllBookingByUserAndState(booker.getId(), "CURRENT", 0, 20);
        Assertions.assertThat(list4).isNotEmpty().hasSize(2);

        List<BookingOutDto> list5 = bookingService.findAllBookingByUserAndState(booker.getId(), "REJECTED", 0, 20);
        Assertions.assertThat(list5).isNotEmpty().hasSize(1);

        List<BookingOutDto> list6 = bookingService.findAllBookingByUserAndState(booker.getId(), "WAITING", 0, 20);
        Assertions.assertThat(list6).isNotEmpty().hasSize(1);
    }
}
