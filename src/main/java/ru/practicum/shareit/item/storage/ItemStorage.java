package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);

    void checkUserAccessToItem(Long userId, Long itemId) throws AccessDeniedException;

    Item getItemById(Long itemId);

    List<Item> findAll(Long userId);

    List<Item> findItemsByQueryText(String queryText);
}
