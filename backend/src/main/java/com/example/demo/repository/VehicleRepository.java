package com.example.demo.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Vehicle;

public interface VehicleRepository  extends JpaRepository<Vehicle, Serializable>{
    
    Vehicle findByLicensePlate(String licensePlate);
}
