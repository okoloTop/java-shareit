package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingRepository {
    private final UserService userService;
    private final BookingMapper bookingMapper;
    private final ru.practicum.shareit.booking.repository.BookingRepository bookingRepository;
    private final ItemService itemService;

    @Override
    public BookingOutDto createBooking(Long userId, BookingDto bookingInDto) {
        userService.findUserById(userId);
        ItemDto itemDto = itemService.findItemById(bookingInDto.getItemId());
        if (!itemDto.getAvailable()) {
            throw new AccessException("Нет доступа к этой вещи");
        }
        if (userId.equals(itemDto.getOwner())) {
            throw new FoundException("Нельзя бронировать свою вещь");
        }
        checkCorrectDateTime(bookingInDto);
        Booking booking = bookingMapper.dtoToBooking(bookingInDto);
        booking.setStatus(Status.WAITING);
        booking.setBooker(userId);
        booking = bookingRepository.save(booking);
        BookingOutDto bookingOutDto = getBookingOutDtoWithItemAndUser(booking);

        return bookingOutDto;
    }

    private static void checkCorrectDateTime(BookingDto bookingInDto) {
        if (bookingInDto.getStart() == null || bookingInDto.getEnd() == null) {
            throw new AccessException("Укажите корректные даты бронирования вещи");
        }
        if (bookingInDto.getStart().equals(bookingInDto.getEnd())) {
            throw new AccessException("Дата начала бронирования и окончания не должна совпадать");
        }
        if (bookingInDto.getStart().isBefore(LocalDateTime.now())) {
            throw new AccessException("Дата начала бронирования должна быть в будущем");
        }
        if (bookingInDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new AccessException("Дата окончания бронирования должна быть в будущем");
        }
        if (bookingInDto.getEnd().isBefore(bookingInDto.getStart())) {
            throw new AccessException("Некорректные даты бронирования");
        }
    }

    @Override
    public BookingOutDto updateBookingApproveStatus(Long userId, Long bookingId, String bookingStatus) {
        userService.findUserById(userId);
        checkBookingExist(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();
        checkItemOwnerForAccess(userId, booking);
        if (booking.getStatus() != Status.WAITING) {
            throw new AccessException("Неверный статус бронирования");
        }

        if ("true".equals(bookingStatus)) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        booking = bookingRepository.save(booking);
        BookingOutDto bookingOutDto = getBookingOutDtoWithItemAndUser(booking);

        return bookingOutDto;
    }

    private void checkItemOwnerForAccess(Long userId, Booking booking) {
        ItemDto itemDto = itemService.findItemById(booking.getItemId());
        if (!userId.equals(itemDto.getOwner())) {
            throw new FoundException("Доступ запрещен");
        }
    }

    @Override
    public BookingOutDto findBookingById(Long userId, Long bookingId) {
        userService.findUserById(userId);
        checkBookingExist(bookingId);
        Optional<Booking> booking = bookingRepository.findByIdAndBookerOrOwner(bookingId, userId);
        if (booking.isEmpty()) {
            throw new FoundException("Бронирование не найдено");
        }
        BookingOutDto bookingOutDto = getBookingOutDtoWithItemAndUser(booking.get());

        return bookingOutDto;
    }

    @Override
    public List<BookingOutDto> findAllBookingByUserAndState(Long userId, String state) {
        checkStateValue(state);
        userService.findUserById(userId);
        List<Booking> bookingList = new ArrayList<>();
        switch (state) {
            case ("ALL"):
                bookingList = bookingRepository.findAllByBookerOrderByStartDesc(userId);
                break;
            case ("CURRENT"):
                bookingList = bookingRepository.findAllByBookerByDateIntoPeriodOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case ("PAST"):
                bookingList = bookingRepository.findAllByBookerAndEndIsBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case ("FUTURE"):
                bookingList = bookingRepository.findAllByBookerAndStartIsAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case ("REJECTED"):
                bookingList = bookingRepository.findAllByBookerAndStatusRejectedOrderByStartDesc(userId);
                break;
            case ("WAITING"):
                bookingList = bookingRepository.findAllByBookerAndStatusWaitingOrderByStartDesc(userId);
                break;
            default:
        }
        return bookingListToOutDtoList(bookingList);
    }

    private static void checkStateValue(String state) {
        List<String> stateList = List.of("ALL", "CURRENT", "PAST", "FUTURE", "REJECTED", "WAITING");
        if (!stateList.contains(state)) {
            throw new AccessException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingOutDto> findAllBookingByOwnerAndState(Long ownerId, String state) {
        checkStateValue(state);
        userService.findUserById(ownerId);
        List<Booking> bookingList = new ArrayList<>();
        switch (state) {
            case ("ALL"):
                bookingList = bookingRepository.findAllByItemOwnerOrderByStartDesc(ownerId);
                break;
            case ("CURRENT"):
                bookingList = bookingRepository.findAllByItemOwnerByDateIntoPeriodOrderByStartDesc(ownerId, LocalDateTime.now());
                break;
            case ("PAST"):
                bookingList = bookingRepository.findAllByItemOwnerAndEndIsBeforeOrderByStartDesc(ownerId, LocalDateTime.now());
                break;
            case ("FUTURE"):
                bookingList = bookingRepository.findAllByItemOwnerAndStartIsAfterOrderByStartDesc(ownerId, LocalDateTime.now());
                break;
            case ("REJECTED"):
                bookingList = bookingRepository.findAllByItemOwnerAndStateRejectedOrderByStartDesc(ownerId);
                break;
            case ("WAITING"):
                bookingList = bookingRepository.findAllByItemOwnerAndStateWaitingOrderByStartDesc(ownerId);
                break;
            default:
        }
        return bookingListToOutDtoList(bookingList);
    }

    @Override
    public List<BookingOutDto> findAllBookingByOwnerIdAndItemId(Long itemId) {
        return bookingListToOutDtoList(bookingRepository.findAllAndItemIdOrderByStartAsc(itemId));
    }

    private List<BookingOutDto> bookingListToOutDtoList(List<Booking> bookingList) {
        List<BookingOutDto> bookingOutDtoList = new ArrayList<>();
        for (Booking booking : bookingList) {
            bookingOutDtoList.add(getBookingOutDtoWithItemAndUser(booking));
        }
        return bookingOutDtoList;
    }

    private BookingOutDto getBookingOutDtoWithItemAndUser(Booking booking) {
        BookingOutDto bookingOutDto = bookingMapper.bookingToDto(booking);
        bookingOutDto.setItem(itemService.findItemById(booking.getItemId()));
        bookingOutDto.setBooker(userService.findUserById(booking.getBooker()));
        return bookingOutDto;
    }

    private void checkBookingExist(Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new FoundException("Бронирование по идентификатору не найдено");
        }
    }

    @Override
    public List<Booking> findAllBookingByUserIdAndItemId(Long userId, Long itemId, LocalDateTime dateTime) {
        return bookingRepository.findAllByItemUserIdAndItemIdOrderByStartDesc(userId, itemId, dateTime);
    }

}
