package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestInDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {
    private final RequestService requestService;

    @PostMapping
    public RequestDto createItemRequest(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                        @Valid @RequestBody RequestInDto requestInDto) {
        log.info("POST /requests - добавление запроса на вещь пользователем {}", userId);
        return requestService.createItemRequest(userId, requestInDto);
    }

    @GetMapping
    public List<RequestDto> findAllRequestByUserId(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                   @PositiveOrZero
                                                   @RequestParam(defaultValue = "0") Integer from,
                                                   @Positive
                                                   @RequestParam(defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        log.info("GET /requests - список запросов вещей пользователя {}", userId);
        return requestService.findAllRequestByUserId(userId, from, size);
    }

    @GetMapping("{requestId}")
    public RequestDto getRequestById(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                     @NotNull @PathVariable Long requestId) {
        log.info("GET /requests/{} - информация о запросе", userId);
        return requestService.getRequestById(userId, requestId);
    }

    @GetMapping("all")
    public List<RequestDto> getPageableRequestById(@NotNull @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                   @PositiveOrZero
                                                   @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                   @Positive
                                                   @RequestParam(name = "size", defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        log.info("GET /requests/all - список запросов");
        return requestService.getPageableRequestById(userId, from, size);
    }

}
