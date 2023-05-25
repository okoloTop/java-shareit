package ru.practicum.shareit.item.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "text")
    private String text;
    @Column(name = "item_id")
    private Long item;
    @Column(name = "author_id")
    private Long authorId;
    @Column(name = "created")
    private LocalDateTime created;
}
