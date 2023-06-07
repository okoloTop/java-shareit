package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestInDto;
import ru.practicum.shareit.request.model.ItemRequest;

public class RequestMapper {

    public static RequestDto requestToDto(ItemRequest request) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(request.getId());
        requestDto.setDescription(request.getDescription());
        requestDto.setCreated(request.getCreated());

        return requestDto;
    }

    public static ItemRequest dtoToItemRequest(RequestInDto requestInDto) {
        ItemRequest request = new ItemRequest();
        request.setDescription(requestInDto.getDescription());
        return request;
    }
}

