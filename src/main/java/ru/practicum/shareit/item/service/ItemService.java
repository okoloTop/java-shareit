package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) throws AccessDeniedException;

    ItemDto findItemById(Long itemId);

    List<ItemDto> findAllByUserId(Long userId);

    List<ItemDto> findItemsByQueryText(String queryText);
}
