package com.example.HallBookingAPP.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Venue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String location;
	private long capacity;
	private double pricePerHour;
	private double amenities;

	Venue() {
	}

	public Venue(String name, String location, long capacity, double pricePerHour, double amenities) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.pricePerHour = pricePerHour;
		this.amenities = amenities;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public long getCapacity() {
		return capacity;
	}

	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}

	public double getPricePerHour() {
		return pricePerHour;
	}

	public void setPricePerHour(double pricePerHour) {
		this.pricePerHour = pricePerHour;
	}

	public double getAmenities() {
		return amenities;
	}

	public void setAmenities(double amenities) {
		this.amenities = amenities;
	}
}