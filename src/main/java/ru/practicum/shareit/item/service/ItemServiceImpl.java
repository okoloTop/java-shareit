package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new InvalidParameterException("Нет значения параметра");
        }
        checkBlankParameter(itemDto.getName());
        checkBlankParameter(itemDto.getDescription());
        UserDto userDto = userService.findUserById(userId);
        Item item = itemMapper.dtoToItem(itemDto);
        item.setOwner(userMapper.dtoToUser(userDto));
        item = itemRepository.createItem(item);
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
        UserDto userDto = userService.findUserById(userId);
        itemRepository.checkUserAccessToItem(userMapper.dtoToUser(userDto), itemId);
        itemDto.setId(itemId);
        Item item = itemMapper.dtoToItem(itemDto);
        item = itemRepository.updateItem(item);
        return itemMapper.itemToDto(item);
    }

    @Override
    public ItemDto findItemById(Long itemId) {
        return itemMapper.itemToDto(itemRepository.getItemById(itemId));
    }

    @Override
    public List<ItemDto> findAllByUserId(Long userId) {
        UserDto userDto = userService.findUserById(userId);
        return itemListToDto(itemRepository.findAll(userDto.getId()));
    }

    @Override
    public List<ItemDto> findItemsByQueryText(String queryText) {
        if (queryText.trim().isBlank()) {
            return new ArrayList<>();
        }
        return itemListToDto(itemRepository.findItemsByQueryText(queryText));
    }

    private List<ItemDto> itemListToDto(List<Item> itemList) {
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemDtoList.add(itemMapper.itemToDto(item));
        }
        return itemDtoList;
    }
}

