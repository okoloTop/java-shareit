package ru.practicum.shareit.user.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CreateUserDto {

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @Email
    private String email;
}
