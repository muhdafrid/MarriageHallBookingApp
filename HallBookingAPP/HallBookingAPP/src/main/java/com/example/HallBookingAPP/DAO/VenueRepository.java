package com.example.HallBookingAPP.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.HallBookingAPP.model.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {

	List<Venue> findAllByLocation(String location);

	List<Venue> findAllByLocationAndCapacityGreaterThanEqual(String location, int capacity);

	List<Venue> findAllByCapacityGreaterThanEqual(int capacity);

	List<Venue> findAllById(Long venueId);

}