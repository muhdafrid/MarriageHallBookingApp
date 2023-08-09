package com.example.HallBookingAPP.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.HallBookingAPP.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	List<User> findAllByEmail(String email);

}