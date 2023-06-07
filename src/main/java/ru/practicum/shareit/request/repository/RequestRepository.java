package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequestorId(Pageable pageable, Long requestorId);

    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Pageable pageable, Long requestorId);
}

