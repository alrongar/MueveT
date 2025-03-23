package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
@Entity
public class Booking {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user")
	private User user;

	@ManyToOne
	@JoinColumn(name = "vehicle")
	private Vehicle Vehicle;

    private LocalDate bookingDate;
	private String status;

    public Booking(User user, Vehicle Vehicle, LocalDate bookingDate, String status) {
		super();
		this.user = user;
		this.Vehicle = Vehicle;
		this.bookingDate = bookingDate;
		this.status = status;
	}

	public Booking() {
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Vehicle getVehicle() {
        return Vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        Vehicle = vehicle;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    

}
