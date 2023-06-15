package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestInDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor

@Slf4j
public class ItemRequestController {
    private final RequestService requestService;

    @PostMapping
    public RequestDto createItemRequest(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                        @RequestBody RequestInDto requestInDto) {
        log.info("POST /requests - добавление запроса на вещь пользователем {}", userId);
        return requestService.createItemRequest(userId, requestInDto);
    }

    @GetMapping
    public List<RequestDto> findAllRequestByUserId(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "0") Integer from,
                                                   @RequestParam(defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        log.info("GET /requests - список запросов вещей пользователя {}", userId);
        return requestService.findAllRequestByUserId(userId, from, size);
    }

    @GetMapping("{requestId}")
    public RequestDto getRequestById(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                     @PathVariable Long requestId) {
        log.info("GET /requests/{} - информация о запросе", userId);
        return requestService.getRequestById(userId, requestId);
    }

    @GetMapping("all")
    public List<RequestDto> getPageableRequestById(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                   @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                   @RequestParam(name = "size", defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        log.info("GET /requests/all - список запросов");
        return requestService.getPageableRequestByUserId(userId, from, size);
    }

}
