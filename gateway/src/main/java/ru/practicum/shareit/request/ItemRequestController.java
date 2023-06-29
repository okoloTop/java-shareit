package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                    @Valid @RequestBody ItemRequestDto requestInDto) {
        log.info("POST /requests - добавление запроса на вещь пользователем {}", userId);
        return requestClient.saveItemRequest(userId, requestInDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllRequestByUserId(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                         @PositiveOrZero
                                                         @RequestParam(defaultValue = "0") Integer from,
                                                         @Positive
                                                         @RequestParam(defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        log.info("GET /requests - список запросов вещей пользователя {}", userId);
        return requestClient.findAllByRequestor(userId, from, size);
    }

    @GetMapping("{requestId}")
    public ResponseEntity<Object> getRequestById(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                 @NotNull @PathVariable Long requestId) {
        log.info("GET /requests/{} - информация о запросе", userId);
        return requestClient.getItemRequest(userId, requestId);
    }

    @GetMapping("all")
    public ResponseEntity<Object> getPageableRequestById(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                         @PositiveOrZero
                                                         @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                         @Positive
                                                         @RequestParam(name = "size", defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        log.info("GET /requests/all - список запросов");
        return requestClient.findItemRequests(userId, from, size);
    }

}
