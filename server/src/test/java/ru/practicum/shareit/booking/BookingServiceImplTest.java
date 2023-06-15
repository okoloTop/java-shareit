package ru.practicum.shareit.booking;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.Constants.DATE_TIME_FORMATTER;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User user2;
    private Item item;
    private BookingDto bookingNewDto;
    private Booking booking;
    private BookingOutDto bookingDtoMastBe;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).name("user").email("user@example.com")
                .build();

        user2 = User.builder()
                .id(2L).name("user2").email("user2@example.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("молоток")
                .description("стальной молоток")
                .owner(new User(1L, "owner", "owner@example.com"))
                .available(true)
                .build();

        bookingNewDto = new BookingDto();
        bookingNewDto.setItemId(1L);
        bookingNewDto.setStart(now.plusDays(2).withNano(0));
        bookingNewDto.setEnd(now.plusDays(3).withNano(0));

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.parse(now.plusDays(2).format(DATE_TIME_FORMATTER)))
                .end(LocalDateTime.parse(now.plusDays(3).format(DATE_TIME_FORMATTER)))
                .booker(user)
                .item(item)
                .status(Status.WAITING)
                .build();

        bookingDtoMastBe = new BookingOutDto();
        bookingDtoMastBe.setId(1L);
        bookingDtoMastBe.setBooker(new BookingOutDto.Booker(user.getId(), user.getName()));
        bookingDtoMastBe.setItem(new BookingOutDto.Item(item.getId(), item.getName()));
        bookingDtoMastBe.setStatus(Status.WAITING);
        bookingDtoMastBe.setStart(LocalDateTime.parse(now.plusDays(2).format(DATE_TIME_FORMATTER)));
        bookingDtoMastBe.setEnd(LocalDateTime.parse(now.plusDays(3).format(DATE_TIME_FORMATTER)));
    }

    @Test
    void createBookingWrongUserIdTest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Throwable throwable = Assertions.catchException(() -> bookingService.createBooking(99L, bookingNewDto));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage("Пользователь не найден");

        Mockito.verify(userRepository, times(1)).findById(anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createBookingWrongItemIdTest() {
        bookingNewDto.setItemId(99L);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Throwable throwable = Assertions.catchException(() -> bookingService.createBooking(1L, bookingNewDto));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage("Вещь не найдена");

        Mockito.verify(userRepository, times(1)).findById(anyLong());
        Mockito.verify(itemRepository, times(1))
                .findById(anyLong());
        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void createBookingItemAvailableFalseTest() {
        item.setAvailable(false);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        Throwable throwable = Assertions.catchException(() -> bookingService.createBooking(1L, bookingNewDto));

        Assertions.assertThat(throwable)
                .isInstanceOf(AccessException.class)
                .hasMessage("Нет доступа к этой вещи");
    }

    @Test
    void createBookingNormalTest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        BookingOutDto saved = bookingService.createBooking(2L, bookingNewDto);

        Assertions.assertThat(saved).isNotNull();
        Assertions.assertThat(saved.getId()).isEqualTo(1L);
        Assertions.assertThat(saved.getBooker().getId()).isEqualTo(1L);
        Assertions.assertThat(saved.getItem().getId()).isEqualTo(1L);

        Assertions.assertThat(saved)
                .usingRecursiveComparison()
                .isEqualTo(bookingDtoMastBe);
    }

    @Test
    void updateBookingApproveStatusNormalTest() {
        bookingDtoMastBe.setStatus(Status.APPROVED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        BookingOutDto updated = bookingService.updateBookingApproveStatus(1L, 1L, true);
        Assertions.assertThat(updated).isNotNull();
        Assertions.assertThat(updated.getId()).isEqualTo(1L);
        Assertions.assertThat(updated.getBooker().getId()).isEqualTo(1L);
        Assertions.assertThat(updated.getItem().getId()).isEqualTo(1L);

        Assertions.assertThat(updated)
                .usingRecursiveComparison()
                .isEqualTo(bookingDtoMastBe);
    }

    @Test
    void updateBookingWrongStatusTest() {
        booking.setStatus(Status.APPROVED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        Throwable throwable = Assertions.catchException(() -> bookingService
                .updateBookingApproveStatus(1L, 1L, true));

        Assertions.assertThat(throwable)
                .isInstanceOf(AccessException.class)
                .hasMessage("Неверный статус бронирования");
    }

    @Test
    void approveNotOwnerTest() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user2));

        Throwable throwable = Assertions.catchException(() -> bookingService
                .updateBookingApproveStatus(2L, 1L, true));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage("Доступ запрещен");
    }

    @Test
    void approveRejectStatusTest() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingOutDto result = bookingService.updateBookingApproveStatus(1L, 1L, false);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void findBookingByWrongIdTest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        Throwable throwable = Assertions.catchException(() -> bookingService.findBookingById(99L, 100L));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage("Бронирование по идентификатору не найдено");
    }

    @Test
    void findBookingByIdNormalTest() {
        when(bookingRepository.findByIdAndBookerOrOwner(anyLong(), anyLong()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        BookingOutDto result = bookingService.findBookingById(1L, 1L);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).usingRecursiveComparison()
                .isEqualTo(bookingDtoMastBe);
    }

    @Test
    void getAllBookingsByUserNormalTest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findAllByBookerIdOrderByStartDesc(any(PageRequest.class), anyLong()))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdByDateIntoPeriodOrderByStartDesc(any(PageRequest.class), anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(any(PageRequest.class), anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(any(PageRequest.class), anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStatusRejectedOrderByStartDesc(any(PageRequest.class), anyLong()))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStatusWaitingOrderByStartDesc(any(PageRequest.class), anyLong()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> listAll = bookingService.findAllBookingByUserAndState(1L, "ALL", 0, 20);
        Assertions.assertThat(listAll).hasSize(1);

        List<BookingOutDto> listFuture = bookingService.findAllBookingByUserAndState(1L, "FUTURE", 0, 20);
        Assertions.assertThat(listFuture).hasSize(1);

        List<BookingOutDto> listPast = bookingService.findAllBookingByUserAndState(1L, "PAST", 0, 20);
        Assertions.assertThat(listPast).hasSize(1);

        List<BookingOutDto> listReject = bookingService.findAllBookingByUserAndState(1L, "REJECTED", 0, 20);
        Assertions.assertThat(listReject).hasSize(1);

        List<BookingOutDto> listCurrent = bookingService.findAllBookingByUserAndState(1L, "CURRENT", 0, 20);
        Assertions.assertThat(listCurrent).hasSize(1);

        List<BookingOutDto> listWaiting = bookingService.findAllBookingByUserAndState(1L, "WAITING", 0, 20);
        Assertions.assertThat(listWaiting).hasSize(1);
    }

    @Test
    void findAllBookingByOwnerAndStateNormalTest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findAllByItemOwnerOrderByStartDesc(any(PageRequest.class), anyLong()))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerByDateIntoPeriodOrderByStartDesc(any(PageRequest.class), anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndEndIsBeforeOrderByStartDesc(any(PageRequest.class), anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndStartIsAfterOrderByStartDesc(any(PageRequest.class), anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndStateRejectedOrderByStartDesc(any(PageRequest.class), anyLong()))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndStateWaitingOrderByStartDesc(any(PageRequest.class), anyLong()))
                .thenReturn(List.of(booking));

        List<BookingOutDto> listAll = bookingService.findAllBookingByOwnerAndState(1L, "ALL", 0, 20);
        Assertions.assertThat(listAll).hasSize(1);

        List<BookingOutDto> listFuture = bookingService.findAllBookingByOwnerAndState(1L, "FUTURE", 0, 20);
        Assertions.assertThat(listFuture).hasSize(1);

        List<BookingOutDto> listPast = bookingService.findAllBookingByOwnerAndState(1L, "PAST", 0, 20);
        Assertions.assertThat(listPast).hasSize(1);

        List<BookingOutDto> listReject = bookingService.findAllBookingByOwnerAndState(1L, "REJECTED", 0, 20);
        Assertions.assertThat(listReject).hasSize(1);

        List<BookingOutDto> listCurrent = bookingService.findAllBookingByOwnerAndState(1L, "CURRENT", 0, 20);
        Assertions.assertThat(listCurrent).hasSize(1);

        List<BookingOutDto> listWaiting = bookingService.findAllBookingByOwnerAndState(1L, "WAITING", 0, 20);
        Assertions.assertThat(listWaiting).hasSize(1);
    }

    @Test
    void createBookerIsEqualOwnerTest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Throwable throwable = Assertions.catchException(() -> bookingService.createBooking(1L, bookingNewDto));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage("Нельзя бронировать свою вещь");
    }
}
