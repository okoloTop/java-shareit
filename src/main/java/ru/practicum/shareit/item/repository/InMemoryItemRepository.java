package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final UserRepository userStorage;
    private Long itemId;

    public InMemoryItemRepository(UserRepository userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public Item createItem(Item item) {
        long newItemId = getItemId();
        item.setId(newItemId);
        items.put(newItemId, item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Long itemId = item.getId();
        checkItemExist(itemId);
        Item updateItem = items.get(itemId);
        if (item.getAvailable() != null) {
            updateItem.setAvailable(item.getAvailable());
        }
        if (item.getName() != null) {
            updateItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updateItem.setDescription(item.getDescription());
        }
        items.put(itemId, updateItem);
        return updateItem;
    }

    private void checkItemExist(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new FoundException("Вещь не найдена");
        }
    }

    @Override
    public void checkUserAccessToItem(User user, Long itemId) throws AccessDeniedException {
        checkItemExist(itemId);
        Item item = items.get(itemId);
        if (!item.getOwner().equals(user)) {
            throw new AccessDeniedException("Вам отказано в доступе к этой вещи");
        }
    }

    @Override
    public Item getItemById(Long itemId) {
        checkItemExist(itemId);
        return items.get(itemId);
    }

    @Override
    public List<Item> findAll(Long userId) {
        return items.values().stream().filter(item -> item.getOwner().equals(userStorage.getUserById(userId))).collect(Collectors.toList());
    }

    @Override
    public List<Item> findItemsByQueryText(String queryText) {
        String lowerCaseQueryText = queryText.toLowerCase();
        return items.values().stream().filter(item -> isQueryExist(item, lowerCaseQueryText)).collect(Collectors.toList());
    }

    private boolean isQueryExist(Item item, String lowerCaseQueryText) {
        return item.getAvailable() && (item.getName().toLowerCase().contains(lowerCaseQueryText) || item.getDescription().toLowerCase().contains(lowerCaseQueryText));
    }

    private long getItemId() {
        if (itemId == null) {
            itemId = 0L;
        }
        itemId++;
        return itemId;
    }
}
