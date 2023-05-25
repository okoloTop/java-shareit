package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserRepository userRepository;

    public CommentDto commentToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(getCommentAuthorName(comment));
        commentDto.setCreated(comment.getCreated());
        return commentDto;
    }

    public List<CommentDto> commentListToDto(List<Comment> commentList) {
        List<CommentDto> commentDtoList = new ArrayList<>();
        for (Comment comment : commentList) {
            commentDtoList.add(commentToDto(comment));
        }
        return commentDtoList;
    }

    private String getCommentAuthorName(Comment comment) {
        Optional<User> user = userRepository.findUserById(comment.getAuthorId());
        String name = user.get().getName();
        return name;
    }
}
