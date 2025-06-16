package com.example.demo.servicesImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Vehicle;
import com.example.demo.entity.VehicleType;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.services.VehicleService;

import jakarta.persistence.EntityNotFoundException;

@Service("vehicleService")
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    @Qualifier("vehicleRepository")
    private VehicleRepository vehicleRepository;

    @Override
    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public List<Vehicle> getAllVehicles() {

        List<Vehicle> vehicles = vehicleRepository.findAll();

        return vehicles;
    }

    @Override
    public void deleteVehicle(String licensePlate) {

        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate);
        vehicleRepository.delete(vehicle);
    }

    @Override
    public void toggleAvailable(String licensePlate) {

        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate);

        if (vehicle != null) {
            vehicle.setAvailable(!vehicle.isAvailable()); 
            vehicleRepository.save(vehicle);
        } else {
            throw new EntityNotFoundException("No se encontró un vehículo con la placa: " + licensePlate);
        }
    }

    @Override
    public boolean existByLicensePlate(String licensePlate) {

        if (vehicleRepository.findByLicensePlate(licensePlate) != null) {
            return true;
        }

        return false;
    }

    @Override
    public Vehicle findBylicensePlate(String licensePlate) {

        return vehicleRepository.findByLicensePlate(licensePlate);
    }

    @Override
    public List<Vehicle> findVehicles(String brand, String model, Integer yearMin, Integer yearMax, Boolean available,
            VehicleType type) {

        List<Vehicle> vehicles = vehicleRepository.findAll();

        return vehicles.stream()
                .filter(v -> (brand == null || v.getBrand().toLowerCase().contains(brand.toLowerCase())))
                .filter(v -> (model == null || v.getModel().toLowerCase().contains(model.toLowerCase())))
                .filter(v -> (yearMin == null || v.getYear() >= yearMin))
                .filter(v -> (yearMax == null || v.getYear() <= yearMax))
                .filter(v -> (available == null || v.isAvailable() == available))
                .filter(v -> (type == null || v.getType() == type))
                .collect(Collectors.toList());
    }
}
