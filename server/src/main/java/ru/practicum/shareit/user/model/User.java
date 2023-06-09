package ru.practicum.shareit.user.model;

import lombok.*;

import javax.persistence.*;


@Entity
@Table(name = "users", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
}
