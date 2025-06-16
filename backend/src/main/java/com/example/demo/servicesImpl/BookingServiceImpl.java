package com.example.demo.servicesImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.services.BookingService;

@Service("bookingService")
public class BookingServiceImpl implements BookingService{

    @Autowired
    @Qualifier("bookingRepository")
    private BookingRepository bookingRepository;


    @Override
    public List<Booking> findBookings(String userEmail, String licensePlate, LocalDate dateMin, LocalDate dateMax, String status) {

        List<Booking> bookings = bookingRepository.findAll();
        
        return bookings.stream()
                .filter(v -> (userEmail == null || v.getUser().getEmail().toLowerCase().contains(userEmail.toLowerCase())))
                .filter(v -> (licensePlate == null || v.getVehicle().getLicensePlate().toLowerCase().contains(licensePlate.toLowerCase())))
                .filter(v -> (dateMin == null || !v.getEndDate().isBefore(dateMin)))
                .filter(v -> (dateMax == null || !v.getStartDate().isAfter(dateMax)))
                .filter(v -> (status == null || v.getStatus().equalsIgnoreCase(status)))
                .collect(Collectors.toList());
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Override
    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
    }
    
    //para evitar hacer una reserva en un plazo reservado sobre el mismo vehiculo
    @Override
    public boolean hasConflictingBooking(Booking newBooking) {
        List<Booking> existingBookings = bookingRepository.findByVehicle(newBooking.getVehicle());
    
        return existingBookings.stream()
            .filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()))
            .anyMatch(b ->
                !newBooking.getEndDate().isBefore(b.getStartDate()) &&
                !newBooking.getStartDate().isAfter(b.getEndDate())
            );
    }


    @Override
    public Booking findById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }


    @Override
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking != null) {
            booking.setStatus("FINISHED");
            bookingRepository.save(booking);
        }
    }


    @Override
    public List<Booking> getActiveBookings(User user) {

        return bookingRepository.findByUserAndStatusNot(user, "FINISHED");
    }

    
}
