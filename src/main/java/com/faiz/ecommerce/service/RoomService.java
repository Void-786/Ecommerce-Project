package com.faiz.ecommerce.service;

import com.faiz.ecommerce.exception.InternalServerException;
import com.faiz.ecommerce.exception.ResourceNotFoundException;
import com.faiz.ecommerce.model.Room;
import com.faiz.ecommerce.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepo;

    public Room addNewRoom(MultipartFile file, String roomType, Double roomPrice) throws IOException, SQLException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        if(file != null) {
            byte[] photoBytes = file.getBytes();
            Blob photoBlob = new SerialBlob(photoBytes);
            room.setPhoto(photoBlob);
        }
        return roomRepo.save(room);
    }


    public List<String> getAllRoomTypes() {
        return roomRepo.findDistinctRoomType();
    }

    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }

    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {
        Optional<Room> theRoom = roomRepo.findById(roomId);
        if(theRoom.isEmpty()) {
            throw new ResourceNotFoundException("Sorry, room not found!");
        }
        Blob photoBlob = theRoom.get().getPhoto();
        if(photoBlob != null) {
            return photoBlob.getBytes(1, (int) photoBlob.length());
        }
        return null;
    }

    public void deleteRoomById(Long roomId) {
        Optional<Room> theRoom = roomRepo.findById(roomId);
        if(theRoom.isPresent()) {
            roomRepo.deleteById(roomId);
        }
    }

    public Room updateRoom(Long roomId, String roomType, Double roomPrice, byte[] photoBytes) throws SQLException {
        Room room = roomRepo.findById(roomId).get();
        if(roomType != null) room.setRoomType(roomType);
        if(roomPrice != null) room.setRoomPrice(roomPrice);
        if(photoBytes != null && photoBytes.length > 0) {
            try {
                room.setPhoto(new SerialBlob(photoBytes));
            }
            catch (SQLException e) {
                throw new InternalServerException("Fail updating room!");
            }
        }
        return roomRepo.save(room);
    }

    public Optional<Room> getRoomById(Long roomId) {
        return Optional.of(roomRepo.findById(roomId).get());
    }

    public List<Room> getAvailableRooms(Date checkInDate, Date checkOutDate, String roomType) {
        return roomRepo.findAvailableRoomsByDatesAndType(checkInDate, checkOutDate, roomType);
    }
}
