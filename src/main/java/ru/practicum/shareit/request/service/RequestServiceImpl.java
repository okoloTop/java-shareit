package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestInDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;


    @Override
    public RequestDto createItemRequest(Long userId, RequestInDto requestInDto) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        ItemRequest request = RequestMapper.dtoToItemRequest(requestInDto);
        request.setCreated(LocalDateTime.now());
        request.setRequestor(user);
        request = requestRepository.save(request);

        return RequestMapper.requestToDto(request);
    }

    @Override
    public List<RequestDto> findAllRequestByUserId(Long userId, Integer from, Integer size) {
        userRepository.findUserById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        List<RequestDto> requestInDtoList = new ArrayList<>();
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requestList = requestRepository.findAllByRequestorId(pageable, userId);
        for (ItemRequest request : requestList) {
            RequestDto requestDto = RequestMapper.requestToDto(request);
            if (request.getItems() != null) {
                requestDto.setItems(ItemMapper.itemListToDto(request.getItems()));
            }
            requestInDtoList.add(requestDto);
        }
        return requestInDtoList;
    }

    @Override
    public RequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findUserById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new FoundException("Запрос не найден"));
        RequestDto requestDto = RequestMapper.requestToDto(request);
        if (request.getItems() != null) {
            requestDto.setItems(ItemMapper.itemListToDto(request.getItems()));
        }
        return requestDto;
    }

    @Override
    public List<RequestDto> getPageableRequestById(Long userId, Integer from, Integer size) {
        userRepository.findUserById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size);
        List<RequestDto> requestInDtoList = new ArrayList<>();
        List<ItemRequest> requestList = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(pageRequest, userId);
        for (ItemRequest request : requestList) {
            RequestDto requestDto = RequestMapper.requestToDto(request);
            if (request.getItems() != null) {
                requestDto.setItems(ItemMapper.itemListToDto(request.getItems()));
            }
            requestInDtoList.add(requestDto);
        }
        return requestInDtoList;
    }
}
