package com.example.HallBookingAPP.Controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.HallBookingAPP.DAO.AdminRepository;
import com.example.HallBookingAPP.DAO.BookingRepository;
import com.example.HallBookingAPP.DAO.UserRepository;
import com.example.HallBookingAPP.DAO.VenueRepository;
import com.example.HallBookingAPP.model.Admin;
import com.example.HallBookingAPP.model.Booking;
import com.example.HallBookingAPP.model.Venue;

@Controller
public class AdminController {

	@Autowired
	UserRepository repo;

	@Autowired
	VenueRepository vRepo;

	@Autowired
	BookingRepository bRepo;

	@Autowired
	AdminRepository aRepo;

	@RequestMapping("/admin-login")
	String adminLogin() {
		return "adminlogin";
	}

	@PostMapping("/admin-home")
	String adminHome(@RequestParam("email") String Email, @RequestParam("pass") String Pass, Model model) {
		List<Admin> admin = aRepo.findAllByEmail(Email);
		while (!admin.isEmpty()) {
			Admin adminObj = admin.get(0);
			if (adminObj.getEmail().equalsIgnoreCase(Email) && adminObj.getPassword().equals(Pass)) {
				List<Booking> book = bRepo.findAllByPaymentStatus("Pending Approval");
				model.addAttribute("bookings", book);
				List<Venue> venues = vRepo.findAll();
				model.addAttribute("venues", venues);
				return "adminhome";
			} else
				break;
		}
		model.addAttribute("failed", true);
		return "adminlogin";
	}

	@GetMapping("/admin-home/add-venue")
	public String saveVenue(@RequestParam String name, @RequestParam String location, @RequestParam long capacity,
			@RequestParam double pricePerHour, @RequestParam double amenities, Model model) {
		Venue addVenue = new Venue(name, location, capacity, pricePerHour, amenities);
		vRepo.save(addVenue);
		model.addAttribute("failed", true);
		showVenues(model);
		List<Booking> book = bRepo.findAllByPaymentStatus("Pending Approval");
		model.addAttribute("bookings", book);
		return "adminhome";
	}

	@PostMapping("/admin-home/delete-venue")
	String deleteVenue(@RequestParam("venueId") long Id, Model model) {
		try {
			vRepo.deleteById(Id);
			model.addAttribute("deleteMessage", true);
		} catch (DataIntegrityViolationException ex) {
			model.addAttribute("deleteError", true);
		}
		showVenues(model);
		List<Booking> book = bRepo.findAllByPaymentStatus("Pending Approval");
		model.addAttribute("bookings", book);
		return "adminhome";
	}

	@PostMapping("/admin-home/accept-booking")
	String acceptBooking(@RequestParam("bookingId") long Id, Model model) {
		Optional<Booking> book = bRepo.findById(Id);
		Booking booking = book.get();
		booking.setPaymentStatus("Approved");
		bRepo.save(booking);
		List<Booking> bookList = bRepo.findAllByPaymentStatus("Pending Approval");
		model.addAttribute("bookings", bookList);
		showVenues(model);
		return "adminhome";
	}

	@PostMapping("/admin-home/reject-booking")
	String rejectBooking(@RequestParam("bookingId") long Id, Model model) {
		Optional<Booking> book = bRepo.findById(Id);
		if (book.isPresent()) {
			Booking booking = book.get();
			booking.setPaymentStatus("Rejected");
			bRepo.save(booking);
			List<Booking> bookList = bRepo.findAllByPaymentStatus("Pending Approval");
			model.addAttribute("bookings", bookList);
			showVenues(model);
			return "adminhome";
		}
		return "adminhome";
	}
	
	public void showVenues(Model model) {
		List<Venue> venues = vRepo.findAll();
		model.addAttribute("venues", venues);
	}
}