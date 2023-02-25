package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film {

    private long id;

    @NotBlank(message = "Name can't be empty")
    private String name;

    @NotNull
    @Size(min = 1, max = 200, message = "Description size mast be between 1 and 200")
    private String description;

    @NotNull(message = "Date mast be real")
    private LocalDate releaseDate;

    @Positive(message = "Bad duration")
    private long duration;

    private Set<Long> likes;
}