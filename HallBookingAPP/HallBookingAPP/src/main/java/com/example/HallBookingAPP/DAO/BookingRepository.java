package com.example.HallBookingAPP.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.HallBookingAPP.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	List<Booking> findAllByUserId(Long id);

	List<Booking> findAllByPaymentStatus(String string);

}