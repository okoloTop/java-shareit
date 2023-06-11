package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemBookingMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;


    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new InvalidParameterException("Нет значения параметра");
        }
        checkBlankParameter(itemDto.getName());
        checkBlankParameter(itemDto.getDescription());
        User user = userRepository.findById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        Item item = ItemMapper.dtoToItem(itemDto);
        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId()).orElseThrow(() -> new FoundException("Запрос не найден"));
            item.setRequest(request);
        }
        item.setOwner(user);
        return ItemMapper.itemToDto(itemRepository.save(item));
    }

    private static void checkBlankParameter(String value) {
        if (value == null || value.trim().isBlank()) {
            throw new InvalidParameterException("Значение параметра пустое");
        }
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) throws AccessDeniedException {
        if (itemDto.getName() != null) {
            checkBlankParameter(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            checkBlankParameter(itemDto.getDescription());
        }
        userRepository.findById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        Optional<Item> checkItem = itemRepository.findById(itemId);
        if (checkItem.isEmpty()) {
            throw new FoundException("Такой вещи нет в базе");
        }
        if (!checkItem.get().getOwner().getId().equals(userId)) {
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
        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId()).orElseThrow(() -> new FoundException("Запрос не найден"));
            updateItem.setRequest(request);
        }
        return ItemMapper.itemToDto(itemRepository.save(updateItem));
    }

    @Override
    public List<ItemBookingDto> findAllByUserId(Long userId) {
        List<Item> itemList = itemRepository.findAllByOwnerId(userId);
        List<ItemBookingDto> itemDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemDtoList.add(findItemBookingById(item.getId(), item.getOwner().getId()));
        }
        return itemDtoList;
    }

    @Override
    public List<ItemDto> findItemsByQueryText(String queryText) {
        if (queryText.trim().isBlank()) {
            return new ArrayList<>();
        }
        return itemListToDto(itemRepository.findItemByAvailableAndQueryContainWithIgnoreCase(queryText));
    }

    @Override
    public ItemBookingDto findItemBookingById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new FoundException("Вещь не найдена"));
        ItemBookingDto itemBookingDto = ItemBookingMapper.itemToDto(item);
        itemBookingDto.setComments(getCommentDtoList(itemId));
        if (userId.equals(item.getOwner().getId())) {
            List<Booking> bookingLastDtoList
                    = bookingRepository.findAllByItemIdAndOrderByStartDesc(itemId, LocalDateTime.now());
            if (bookingLastDtoList.size() > 0) {
                itemBookingDto.setLastBooking(getBookingItemDto(bookingLastDtoList.get(0)));
            }
            List<Booking> bookingNextDtoList
                    = bookingRepository.findAllByItemIdOrderByStartAsc(itemId, LocalDateTime.now());
            if (bookingNextDtoList.size() > 0) {
                itemBookingDto.setNextBooking(getBookingItemDto(bookingNextDtoList.get(0)));
            }
        }
        return itemBookingDto;
    }

    private static BookingItemDto getBookingItemDto(Booking bookingOutDto) {
        return new BookingItemDto(bookingOutDto.getId(), bookingOutDto.getBooker().getId(),
                bookingOutDto.getStart(), bookingOutDto.getEnd(), bookingOutDto.getStatus());
    }

    private List<ItemDto> itemListToDto(List<Item> itemList) {
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemDtoList.add(ItemMapper.itemToDto(item));
        }
        return itemDtoList;
    }

    @Override
    @Transactional
    public CommentDto addCommentToItem(Long userId, Long itemId, CommentInDto commentInDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        Item item = findFullItemById(itemId);
        List<Booking> bookingList = bookingRepository.findAllByItemUserIdAndItemIdOrderByStartDesc(userId, itemId, LocalDateTime.now());
        if (bookingList.size() == 0) {
            throw new InvalidParameterException("нельзя оставить отзыв этой вещи");
        }
        Comment comment = new Comment();
        comment.setItem(item.getId());
        comment.setAuthorId(user.getId());
        comment.setText(commentInDto.getText());
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);
        CommentDto commentDto = CommentMapper.commentToDto(comment);
        commentDto.setAuthorName(getCommentAuthorName(userId));
        return commentDto;
    }

    @Override
    public Item findFullItemById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new FoundException("Вещь не найдена"));
    }

    private String getCommentAuthorName(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        return user.getName();
    }

    private List<CommentDto> getCommentDtoList(Long itemId) {
        List<Comment> commentList = commentRepository.findAllByCommentAndItemId(itemId);
        List<CommentDto> commentDtoList = CommentMapper.commentListToDto(commentList);
        for (Comment commentList1 : commentList) {
            for (CommentDto commentDto : commentDtoList) {
                commentDto.setAuthorName(getCommentAuthorName(commentList1.getAuthorId()));
            }
        }
        return commentDtoList;
    }
}

