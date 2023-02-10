package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film extends Progenitor {
    @NotBlank(message = "Name can't be empty")
    private String name;
    @Size(min = 1, max = 200, message = "Description size mast be between 1 and 200")
    private String description;
    @NotNull(message = "Date mast be real")
    private LocalDate releaseDate;
    @Min(1)
    private long duration;
}