package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ItemRepository {
    Item createItem(Item item);

    Item updateItem(Item item);

    void checkUserAccessToItem(User user, Long itemId) throws AccessDeniedException;

    Item getItemById(Long itemId);

    List<Item> findAll(Long userId);

    List<Item> findItemsByQueryText(String queryText);
}
