package com.example.demo.services;

import java.util.List;

import com.example.demo.entity.Vehicle;
import com.example.demo.entity.VehicleType;

public interface VehicleService {
    
    Vehicle saveVehicle (Vehicle vehicle);
    
    List<Vehicle> getAllVehicles();

    void deleteVehicle(String licensePlate);

    void toggleAvailable(String licensePlate);

    boolean existByLicensePlate(String licensePlate);

    Vehicle findBylicensePlate(String licensePlate);

    public List<Vehicle> findVehicles(String brand, String model, Integer yearMin, Integer yearMax, Boolean available, VehicleType type);
}
