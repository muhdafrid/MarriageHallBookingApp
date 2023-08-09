package com.example.HallBookingAPP.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.HallBookingAPP.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, String> {

	List<Admin> findAllByEmail(String email);

}