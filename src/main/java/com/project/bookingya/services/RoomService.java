package com.project.bookingya.services;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import com.project.bookingya.dtos.RoomDto;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.EntityExistsException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Room;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.shared.Constants;

@Service
public class RoomService {

    private final IRoomRepository roomRepository;
    private final ModelMapper mapper;

    public RoomService(IRoomRepository roomRepository, ModelMapper mapper) {
        this.roomRepository = roomRepository;
        this.mapper = mapper;
    }

    public Room create(RoomDto roomDto) {
        if (roomRepository.existsByCode(roomDto.getCode())) {
            throw new EntityExistsException(Constants.ROOM_EXISTS);
        }

        RoomEntity entity = mapper.map(roomDto, RoomEntity.class);
        RoomEntity saved = roomRepository.save(entity);
        return mapper.map(saved, Room.class);
    }

    public Room update(RoomDto roomDto, UUID id) {
        RoomEntity existing = roomRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.ROOM_NOT_FOUND));

        if (!existing.getCode().equals(roomDto.getCode()) && roomRepository.existsByCode(roomDto.getCode())) {
            throw new EntityExistsException(Constants.ROOM_EXISTS);
        }

        mapper.map(roomDto, existing);
        RoomEntity updated = roomRepository.save(existing);
        return mapper.map(updated, Room.class);
    }

    public List<Room> getAll() {
        List<RoomEntity> rooms = roomRepository.findAll();
        Type listType = new TypeToken<List<Room>>() {}.getType();
        return mapper.map(rooms, listType);
    }

    public Room getById(UUID id) {
        RoomEntity room = roomRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.ROOM_NOT_FOUND));
        return mapper.map(room, Room.class);
    }

    public Room getByCode(String code) {
        RoomEntity room = roomRepository.findByCode(code)
            .orElseThrow(() -> new EntityNotExistsException(Constants.ROOM_NOT_FOUND));
        return mapper.map(room, Room.class);
    }

    public void delete(UUID id) {
        RoomEntity room = roomRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.ROOM_NOT_FOUND));
        roomRepository.delete(room);
    }
}
