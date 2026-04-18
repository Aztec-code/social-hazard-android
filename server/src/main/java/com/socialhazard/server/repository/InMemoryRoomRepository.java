package com.socialhazard.server.repository;

import com.socialhazard.server.model.domain.GameRoom;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryRoomRepository implements RoomRepository {

    private final ConcurrentMap<String, GameRoom> rooms = new ConcurrentHashMap<>();

    @Override
    public Optional<GameRoom> findByCode(String roomCode) {
        return Optional.ofNullable(rooms.get(roomCode));
    }

    @Override
    public void save(GameRoom room) {
        rooms.put(room.getRoomCode(), room);
    }

    @Override
    public void delete(String roomCode) {
        rooms.remove(roomCode);
    }

    @Override
    public Collection<GameRoom> findAll() {
        return rooms.values();
    }

    @Override
    public long count() {
        return rooms.size();
    }
}
