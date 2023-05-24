package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemBookingMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;
    private final ItemBookingMapper itemBookingMapper;
    private final CommentService commentService;

    @Autowired
    public ItemServiceImpl(UserService userService,
                           ItemRepository itemRepository,
                           @Lazy BookingService bookingService,
                           ItemMapper itemMapper, UserMapper userMapper,
                           ItemBookingMapper itemBookingMapper, @Lazy CommentService commentService) {
        this.userService = userService;
        this.itemRepository = itemRepository;
        this.bookingService = bookingService;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
        this.itemBookingMapper = itemBookingMapper;
        this.commentService = commentService;
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new InvalidParameterException("Нет значения параметра");
        }
        checkBlankParameter(itemDto.getName());
        checkBlankParameter(itemDto.getDescription());
        UserDto userDto = userService.findUserById(userId);
        Item item = itemMapper.dtoToItem(itemDto);
        item.setOwner(userMapper.dtoToUser(userDto).getId());
        item = itemRepository.save(item);
        return itemMapper.itemToDto(item);
    }

    private static void checkBlankParameter(String value) {
        if (value == null || value.trim().isBlank()) {
            throw new InvalidParameterException("Значение параметра пустое");
        }
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) throws AccessDeniedException {
        if (itemDto.getName() != null) {
            checkBlankParameter(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            checkBlankParameter(itemDto.getDescription());
        }
        if (userService.findUserById(userId) == null) {
            throw new FoundException("Пользователь не найден");
        }
        Optional<Item> checkItem = itemRepository.findById(itemId);
        if (checkItem.isEmpty()) {
            throw new FoundException("Такой вещи нет в базе");
        }
        if (!checkItem.get().getOwner().equals(userId)) {
            throw new AccessDeniedException("Вещь не принадлежит этому пользователю");
        }
        Item updateItem = checkItem.get();
        if (itemDto.getAvailable() != null) {
            updateItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getName() != null) {
            updateItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            updateItem.setDescription(itemDto.getDescription());
        }
        return itemMapper.itemToDto(itemRepository.save(updateItem));
    }

    @Override
    public ItemDto findItemById(Long itemId) {
        if (itemRepository.findById(itemId).isEmpty()) {
            throw new FoundException("Такой вещи нет в базе");
        }
        return itemMapper.itemToDto(itemRepository.findById(itemId).get());
    }

    @Override
    public List<ItemBookingDto> findAllByUserId(Long userId) {
        List<Item> itemList = itemRepository.findAllByOwner(userId);
        List<ItemBookingDto> itemBookingDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemBookingDtoList.add(findItemBookingById(userId, item.getId()));
        }
        return itemBookingDtoList;
    }

    @Override
    public List<ItemDto> findItemsByQueryText(String queryText) {
        if (queryText.trim().isBlank()) {
            return new ArrayList<>();
        }
        return itemListToDto(itemRepository.findItemByAvailableAndQueryContainWithIgnoreCase(queryText));
    }

    @Override
    public ItemBookingDto findItemBookingById(Long userId, Long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new FoundException("Вещь не найдена");
        }
        ItemBookingDto itemBookingDto = itemBookingMapper.itemToDto(item.get());
        if (Objects.equals(userId, item.get().getOwner())) {
            List<BookingOutDto> bookingDtoList
                    = bookingService.findAllBookingByOwnerIdAndItemId(itemId);
            if (bookingDtoList.size() > 0) {
                if (bookingDtoList.get(0).getStart().isAfter(LocalDateTime.now())) {
                    itemBookingDto.setNextBooking(getBookingItemDto(bookingDtoList.get(0)));
                } else {
                    itemBookingDto.setLastBooking(getBookingItemDto(bookingDtoList.get(0)));
                }
            }
            if (bookingDtoList.size() > 1) {
                if (bookingDtoList.get(0).getStart().isBefore(LocalDateTime.now()) &&
                        bookingDtoList.get(1).getStart().isBefore(LocalDateTime.now())) {
                    itemBookingDto.setNextBooking((getBookingItemDto(bookingDtoList.get(2))));
                    itemBookingDto.setLastBooking(getBookingItemDto(bookingDtoList.get(1)));
                } else {
                    itemBookingDto.setNextBooking(getBookingItemDto(bookingDtoList.get(1)));
                    itemBookingDto.setLastBooking(getBookingItemDto(bookingDtoList.get(0)));
                }
            }
        }
        return itemBookingDto;
    }

    private static BookingItemDto getBookingItemDto(BookingOutDto bookingOutDto) {
        return new BookingItemDto(bookingOutDto.getId(), bookingOutDto.getBooker().getId(),
                bookingOutDto.getStart(), bookingOutDto.getEnd(), bookingOutDto.getStatus());
    }

    private List<ItemDto> itemListToDto(List<Item> itemList) {
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemDtoList.add(itemMapper.itemToDto(item));
        }
        return itemDtoList;
    }

    @Override
    @Transactional
    public CommentDto addCommentToItem(Long userId, Long itemId, CommentInDto commentInDto) {
        userService.findUserById(userId);
        User user = userService.findFullUserById(userId);
        Item item = findFullItemById(itemId);
        List<Booking> bookingList = bookingService.findAllBookingByUserIdAndItemId(userId, itemId, LocalDateTime.now());
        if (bookingList.size() == 0) {
            throw new InvalidParameterException("нельзя оставить отзыв этой вещи");
        }
        return commentService.addCommentToItem(user, item, commentInDto);
    }

    @Override
    public Item findFullItemById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new FoundException("Вещь не найдена"));
    }
}

