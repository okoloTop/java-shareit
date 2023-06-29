package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) throws AccessDeniedException;

    List<ItemBookingDto> findAllByUserId(Long userId);

    List<ItemDto> findItemsByQueryText(String queryText);

    ItemBookingDto findItemBookingById(Long userId, Long itemId);

    CommentDto addCommentToItem(Long userId, Long itemId, CommentInDto commentInDto);

    Item findFullItemById(Long itemId);

}
