package com.example.demo.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;

public interface BookingRepository extends JpaRepository<Booking, Serializable>{

    List<Booking> findByVehicle(Vehicle vehicle);


    List<Booking> findByUserAndStatusNot(User user, String status);

    
    
}
