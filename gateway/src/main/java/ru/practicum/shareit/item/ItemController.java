package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.file.AccessDeniedException;



@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> findAllItemsByUserId(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long ownerId) {
        log.info("GET /items - получение списка вещей по id пользователя.");
        return itemClient.findAllByUserId(ownerId);
    }

    @PostMapping
    public ResponseEntity<Object>  createItem(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemRequestDto itemDto) {
        log.info("POST /items - создание новой вещи.");
        return itemClient.create(userId, itemDto);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<Object>  findItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.findById(itemId, userId);
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<Object>  updateItem(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                              @NotNull @PathVariable Long itemId,
                              @RequestBody ItemRequestDto itemDto)
            throws AccessDeniedException {
        log.info("PATCH /items - обновление вещи.");
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("search")
    public ResponseEntity<Object>  findItemsByQueryText(@RequestParam(name = "text", defaultValue = "") String queryText) {
        log.info("GET /items search- поиск вещи.");
        return itemClient.search(queryText);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object>  addCommentToItem(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                       @NotNull @PathVariable Long itemId,
                                       @Valid @RequestBody CommentRequestDto commentInDto) {
        log.info("POST {itemId}/comment - добавление комментария");
        return itemClient.addComment(userId, itemId, commentInDto);
    }
}

