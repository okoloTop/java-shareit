package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingOutDto createBooking(Long userId, BookingDto bookingInDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new FoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingInDto.getItemId())
                .orElseThrow(() -> new FoundException("Вещь не найдена"));
        if (!item.getAvailable()) {
            throw new AccessException("Нет доступа к этой вещи");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new FoundException("Нельзя бронировать свою вещь");
        }
        Booking booking = BookingMapper.dtoToBooking(bookingInDto);
        booking.setStatus(Status.WAITING);
        booking.setBooker(booker);
        booking.setItem(item);
        booking = bookingRepository.save(booking);
        BookingOutDto bookingOutDto = getBookingOutDtoWithItemAndUser(booking);

        return bookingOutDto;
    }

    @Override
    @Transactional
    public BookingOutDto updateBookingApproveStatus(Long userId, Long bookingId, Boolean approved) {
        userRepository.findById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        checkBookingExist(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();
        checkItemOwnerForAccess(userId, booking);
        if (booking.getStatus() != Status.WAITING) {
            throw new AccessException("Неверный статус бронирования");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        booking = bookingRepository.save(booking);
        BookingOutDto bookingOutDto = getBookingOutDtoWithItemAndUser(booking);

        return bookingOutDto;
    }

    private void checkItemOwnerForAccess(Long userId, Booking booking) {
        Item item = itemRepository.findById(booking.getItem().getId()).orElseThrow(() -> new FoundException("Вещь не найдена"));
        if (!userId.equals(item.getOwner().getId())) {
            throw new FoundException("Доступ запрещен");
        }
    }

    @Override
    public BookingOutDto findBookingById(Long userId, Long bookingId) {
        userRepository.findById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        checkBookingExist(bookingId);
        Optional<Booking> booking = bookingRepository.findByIdAndBookerOrOwner(bookingId, userId);
        if (booking.isEmpty()) {
            throw new FoundException("Бронирование не найдено");
        }
        BookingOutDto bookingOutDto = getBookingOutDtoWithItemAndUser(booking.get());

        return bookingOutDto;
    }

    @Override
    public List<BookingOutDto> findAllBookingByUserAndState(Long userId, String state, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        List<Booking> bookingList = new ArrayList<>();
        Sort sort = Sort.sort(Booking.class).by(Booking::getStart).descending();
        Pageable pageable = PageRequest.of(from / size, size, sort);
        switch (state) {
            case ("ALL"):
                bookingList = bookingRepository.findAllByBookerIdOrderByStartDesc(pageable, userId);
                break;
            case ("CURRENT"):
                bookingList = bookingRepository.findAllByBookerIdByDateIntoPeriodOrderByStartDesc(pageable, userId, LocalDateTime.now());
                break;
            case ("PAST"):
                bookingList = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(pageable, userId, LocalDateTime.now());
                break;
            case ("FUTURE"):
                bookingList = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(pageable, userId, LocalDateTime.now());
                break;
            case ("REJECTED"):
                bookingList = bookingRepository.findAllByBookerIdAndStatusRejectedOrderByStartDesc(pageable, userId);
                break;
            case ("WAITING"):
                bookingList = bookingRepository.findAllByBookerIdAndStatusWaitingOrderByStartDesc(pageable, userId);
                break;
            default:
        }
        return bookingListToOutDtoList(bookingList);
    }

    @Override
    public List<BookingOutDto> findAllBookingByOwnerAndState(Long ownerId, String state, Integer from, Integer size) {
        userRepository.findById(ownerId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        List<Booking> bookingList = new ArrayList<>();
        Sort sort = Sort.sort(Booking.class).by(Booking::getStart).descending();
        Pageable pageable = PageRequest.of(from / size, size, sort);
        switch (state) {
            case ("ALL"):
                bookingList = bookingRepository.findAllByItemOwnerOrderByStartDesc(pageable, ownerId);
                break;
            case ("CURRENT"):
                bookingList = bookingRepository.findAllByItemOwnerByDateIntoPeriodOrderByStartDesc(pageable, ownerId, LocalDateTime.now());
                break;
            case ("PAST"):
                bookingList = bookingRepository.findAllByItemOwnerAndEndIsBeforeOrderByStartDesc(pageable, ownerId, LocalDateTime.now());
                break;
            case ("FUTURE"):
                bookingList = bookingRepository.findAllByItemOwnerAndStartIsAfterOrderByStartDesc(pageable, ownerId, LocalDateTime.now());
                break;
            case ("REJECTED"):
                bookingList = bookingRepository.findAllByItemOwnerAndStateRejectedOrderByStartDesc(pageable, ownerId);
                break;
            case ("WAITING"):
                bookingList = bookingRepository.findAllByItemOwnerAndStateWaitingOrderByStartDesc(pageable, ownerId);
                break;
            default:
        }
        return bookingListToOutDtoList(bookingList);
    }

    private List<BookingOutDto> bookingListToOutDtoList(List<Booking> bookingList) {
        List<BookingOutDto> bookingOutDtoList = new ArrayList<>();
        for (Booking booking : bookingList) {
            bookingOutDtoList.add(getBookingOutDtoWithItemAndUser(booking));
        }
        return bookingOutDtoList;
    }

    private BookingOutDto getBookingOutDtoWithItemAndUser(Booking booking) {
        BookingOutDto bookingOutDto = BookingMapper.toBookingDto(booking);
        bookingOutDto.setItem(new BookingOutDto.Item(booking.getItem().getId(), booking.getItem().getName()));
        bookingOutDto.setBooker(new BookingOutDto.Booker(booking.getBooker().getId(), booking.getBooker().getName()));
        return bookingOutDto;
    }

    private void checkBookingExist(Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new FoundException("Бронирование по идентификатору не найдено");
        }
    }
}
