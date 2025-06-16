package com.example.demo.services;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;

public interface BookingService {

    public List<Booking> findBookings(String userEmail, String licensePlate, LocalDate dateMin, LocalDate dateMax, String status);

    public Booking getBookingById(Long id);

    public void saveBooking(Booking booking);

    public boolean hasConflictingBooking(Booking newBooking);

    public Booking findById(Long id);

    public void deleteBooking(Long id);

    public List<Booking> getActiveBookings(User user);

}
