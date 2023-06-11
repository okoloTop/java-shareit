package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.file.AccessDeniedException;
import java.util.List;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemBookingDto> findAllItemsByUserId(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long ownerId) {
        log.info("GET /items - получение списка вещей по id пользователя.");
        return itemService.findAllByUserId(ownerId);
    }

    @PostMapping
    public ItemDto createItem(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items - создание новой вещи.");
        return itemService.createItem(userId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemBookingDto findItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.findItemBookingById(itemId, userId);
    }

    @PatchMapping("{itemId}")
    public ItemDto updateItem(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                              @NotNull @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto)
            throws AccessDeniedException {
        log.info("PATCH /items - обновление вещи.");
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("search")
    public List<ItemDto> findItemsByQueryText(@RequestParam(name = "text", defaultValue = "") String queryText) {
        log.info("GET /items search- поиск вещи.");
        return itemService.findItemsByQueryText(queryText);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto addCommentToItem(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                       @NotNull @PathVariable Long itemId,
                                       @Valid @RequestBody CommentInDto commentInDto) {
        log.info("POST {itemId}/comment - добавление комментария");
        return itemService.addCommentToItem(userId, itemId, commentInDto);
    }
}

