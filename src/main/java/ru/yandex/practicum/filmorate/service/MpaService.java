package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ItemDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;

import java.util.Set;

@Service
@Slf4j
public class MpaService {

    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Set<Mpa> getMpas() {
        log.info("GET request - all MPA");
        return mpaStorage.getMpas();
    }

    public Mpa getMpa(int id) {
        log.info("GET request - MPA with id={}", id);
        containsMpa(id);
        return mpaStorage.getMpa(id);
    }

    private void containsMpa(long id) {
        if (!mpaStorage.containsMpa(id)) {
            throw new ItemDoesNotExistException("MPA with id=" + id + " not exist. ");
        }
    }
}