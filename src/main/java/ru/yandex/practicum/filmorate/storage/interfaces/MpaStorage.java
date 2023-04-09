package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Set;

public interface MpaStorage {

    Mpa getMpa(long id);

    Set<Mpa> getMpas();

    long saveMpa(Mpa mpa);

    boolean containsMpa(long id);
}