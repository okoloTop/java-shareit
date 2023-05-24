package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemBookingMapper {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    public ItemBookingDto itemToDto(Item item) {
        ItemBookingDto itemWithBookingDto = new ItemBookingDto();
        itemWithBookingDto.setId(item.getId());
        itemWithBookingDto.setName(item.getName());
        itemWithBookingDto.setDescription(item.getDescription());
        itemWithBookingDto.setAvailable(item.getAvailable());
        itemWithBookingDto.setOwner(item.getOwner());
        itemWithBookingDto.setComments(getCommentDtoList(item.getId()));
        return itemWithBookingDto;
    }

    private List<CommentDto> getCommentDtoList(Long itemId) {
        List<Comment> commentList = commentRepository.findAllByCommentAndItemId(itemId);
        List<CommentDto> commentDtoList = commentMapper.commentListToDto(commentList);

        return commentDtoList;
    }
}
