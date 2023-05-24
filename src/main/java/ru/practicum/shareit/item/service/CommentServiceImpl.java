package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto addCommentToItem(User user, Item item, CommentInDto commentInDto) {
        Comment comment = new Comment();
        comment.setItem(item.getId());
        comment.setAuthor(user.getName());
        comment.setText(commentInDto.getText());
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);
        return commentMapper.commentToDto(comment);
    }
}
