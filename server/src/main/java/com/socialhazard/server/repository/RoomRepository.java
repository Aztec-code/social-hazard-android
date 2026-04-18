package com.socialhazard.server.repository;

import com.socialhazard.server.model.domain.GameRoom;

import java.util.Collection;
import java.util.Optional;

public interface RoomRepository {

    Optional<GameRoom> findByCode(String roomCode);

    void save(GameRoom room);

    void delete(String roomCode);

    Collection<GameRoom> findAll();

    long count();
}
