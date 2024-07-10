package com.faiz.ecommerce.service;

import com.faiz.ecommerce.exception.InvalidBookingRequestException;
import com.faiz.ecommerce.exception.ResourceNotFoundException;
import com.faiz.ecommerce.model.BookedRoom;
import com.faiz.ecommerce.model.Room;
import com.faiz.ecommerce.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final RoomService roomService;

    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepo.findByRoomId(roomId);
    }

    public List<BookedRoom> getAllBookings() {
        return bookingRepo.findAll();
    }

    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if(bookingRequest.getCheckOutDate().toInstant().isBefore(bookingRequest.getCheckOutDate().toInstant())) {
            throw new InvalidBookingRequestException("Check-in date must come before check-out date");
        }
        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);
        if(roomIsAvailable) {
            room.addBooking(bookingRequest);
            bookingRepo.save(bookingRequest);
        }
        else {
            throw new InvalidBookingRequestException("Sorry, this room is not available for the selected dates");
        }
        return bookingRequest.getBookingConfirmationCode();
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                           bookingRequest.getCheckInDate().toInstant().equals(existingBooking.getCheckInDate().toInstant())
                        || bookingRequest.getCheckInDate().toInstant().isBefore(existingBooking.getCheckOutDate().toInstant())
                        || (bookingRequest.getCheckInDate().toInstant().isAfter(existingBooking.getCheckOutDate().toInstant())
                        && bookingRequest.getCheckInDate().toInstant().isBefore(existingBooking.getCheckOutDate().toInstant()))
                        || (bookingRequest.getCheckInDate().toInstant().isBefore(existingBooking.getCheckOutDate().toInstant())
                        && bookingRequest.getCheckInDate().toInstant().equals(existingBooking.getCheckOutDate().toInstant()))
                        || (bookingRequest.getCheckInDate().toInstant().isBefore(existingBooking.getCheckOutDate().toInstant())
                        && bookingRequest.getCheckInDate().toInstant().isAfter(existingBooking.getCheckOutDate().toInstant()))
                        || (bookingRequest.getCheckInDate().toInstant().equals(existingBooking.getCheckOutDate().toInstant())
                        && bookingRequest.getCheckInDate().toInstant().equals(existingBooking.getCheckOutDate().toInstant()))
                        || (bookingRequest.getCheckInDate().toInstant().equals(existingBooking.getCheckOutDate().toInstant())
                        && bookingRequest.getCheckInDate().toInstant().equals(existingBooking.getCheckOutDate().toInstant()))
                );
    }

    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepo.findByBookingConfirmationCode(confirmationCode)
                .orElseThrow(() -> new ResourceNotFoundException("No booking found with booking code " + confirmationCode));
    }

    public List<BookedRoom> getBookingsByUserEmail(String email) {
        return bookingRepo.findByGuestEmail(email);
    }

    public void cancelBooking(Long bookingId) {
        bookingRepo.deleteById(bookingId);
    }
}
