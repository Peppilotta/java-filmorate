package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Progenitor {
    @NotBlank(message = "Email absent")
    @Email(message = "Wrong email")
    private String email;
    @NotBlank(message = "Wrong login")
    private String login;
    private String name;
    @Past
    private LocalDate birthday;
}