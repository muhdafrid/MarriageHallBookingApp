package com.example.HallBookingAPP.Controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.HallBookingAPP.DAO.AdminRepository;
import com.example.HallBookingAPP.DAO.BookingRepository;
import com.example.HallBookingAPP.DAO.UserRepository;
import com.example.HallBookingAPP.DAO.VenueRepository;
import com.example.HallBookingAPP.model.Booking;
import com.example.HallBookingAPP.model.User;
import com.example.HallBookingAPP.model.Venue;

@Controller
public class UserController {

	@Autowired
	UserRepository repo;

	@Autowired
	VenueRepository vRepo;

	@Autowired
	BookingRepository bRepo;

	@Autowired
	AdminRepository aRepo;

	@RequestMapping("/register")
	public String getVenues(Model model) {
		return "register";
	}

	@RequestMapping("/login")
	String login() {
		return "login";
	}

	@PostMapping("/register")
	String register(@RequestParam("name") String name, @RequestParam("email") String email,
			@RequestParam("pass") String pass, Model model) {
		if (strHasInt(name).length() == 0) {
			model.addAttribute("nameError", true);
			return "register";
		}
		if (emailvalidation(email) == false) {
			model.addAttribute("emailError", true);
			return "register";
		}
		if (containsWhiteSpace(pass) == true || pass.length() < 8) {
			model.addAttribute("passError", true);
			return "register";
		}
		if (repo.existsByEmail(email) == true) {
			model.addAttribute("failed", true);
			return "register";
		}
		User user = new User(name, email, pass);
		repo.save(user);
		model.addAttribute("success", true);
		return "register";
	}

	static String customerEmail;
	static Long venueId;

	@PostMapping("/profile")
	String profile(@RequestParam("email") String email, @RequestParam("pass") String pass, Model model) {
		List<User> list = repo.findAllByEmail(email);
		while (!list.isEmpty()) {
			User user = list.get(0);
			List<Booking> bList = bRepo.findAllByUserId(user.getId());
			if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(pass)) {
				UserController.customerEmail = user.getEmail();
				showVenues(model);
				myBookings(model);
				if (!bList.isEmpty()) {
					Booking book = bList.get(0);
					updateBooking(book);
				}
				return "home";
			} else
				break;
		}
		model.addAttribute("failed", true);
		return "login";
	}

	@PostMapping("/")
	String filter(Model model, @RequestParam("location") String location,
			@RequestParam("capacity") String capacityStr) {
		capacityStr.trim();
		int capacity;
		try {
			capacity = Integer.parseInt(capacityStr);
		} catch (NumberFormatException e) {
			capacity = -1;
		}
		if (location.length() > 0 && capacity > -1) {
			List<Venue> venues = vRepo.findAllByLocationAndCapacityGreaterThanEqual(location, capacity);
			model.addAttribute("venues", venues);
			myBookings(model);
			return "home";
		} else if (location.length() > 0 && capacity == -1) {
			List<Venue> venues = vRepo.findAllByLocation(location);
			model.addAttribute("venues", venues);
			myBookings(model);
			return "home";
		} else if (location.length() <= 0 && capacity > -1) {
			List<Venue> venues = vRepo.findAllByCapacityGreaterThanEqual(capacity);
			model.addAttribute("venues", venues);
			myBookings(model);
			return "home";
		} else {
			myBookings(model);
			showVenues(model);
			return "home";
		}
	}

	@PostMapping("/book")
	String book(@RequestParam("venueId") Long venueId, Model model) {
		UserController.venueId = venueId;
		List<User> users = repo.findAllByEmail(customerEmail);
		User user = users.get(0);
		List<Booking> bookings = bRepo.findAllByUserId(user.getId());
		if (!bookings.isEmpty()) {
			model.addAttribute("success", true);
			myBookings(model);
			showVenues(model);
			return "home";
		}
		model.addAttribute("bookForm", true);
		myBookings(model);
		showVenues(model);
		return "home";
	}

	@PostMapping("/booking")
	String booking(@RequestParam("inputStartTime") LocalDateTime startTime,
			@RequestParam("inputEndTime") LocalDateTime endTime, Model model) {
		List<User> users = repo.findAllByEmail(customerEmail);
		List<Venue> venues = vRepo.findAllById(venueId);
		User user = users.get(0);
		Venue venue = venues.get(0);
		List<Booking> book = bRepo.findAllByUserId(user.getId());
		double totalPrice = priceCalc(startTime, endTime, venue);
		if (book.isEmpty() && totalPrice > 0) {
			String paymentStatus = "Pending Approval";
			Booking booking = new Booking(user, venue, startTime, endTime, totalPrice, paymentStatus);
			bRepo.save(booking);
			model.addAttribute("bookingMessage",
					"Booking Succeessfull Waiting for Response! total Price = " + totalPrice);
			showVenues(model);
			myBookings(model);
			return "home";
		} else {
			model.addAttribute("bookingMessage", "Booking Failed Due To Invalid Date.");
			showVenues(model);
			myBookings(model);
			return "home";
		}
	}

	@PostMapping("/profile/my-Bookings/delete")
	String deleteBooking(@RequestParam("BookingId") long Id, Model model) {
		bRepo.deleteById(Id);
		model.addAttribute("DeleteSuccess", true);
		showVenues(model);
		myBookings(model);
		return "home";
	}

	static long Id;

	@PostMapping("/profile/my-Bookings/update")
	String updateBooking(@RequestParam("BookingId") long id, Model model) {
		Id = id;
		showVenues(model);
		myBookings(model);
		model.addAttribute("updateForm", true);
		return "home";
	}

	@PostMapping("/profile/my-Bookings/update-success")
	String updateSuccess(@RequestParam("startTime") LocalDateTime startTime,
			@RequestParam("endTime") LocalDateTime endTime, Model model) {
		Optional<Booking> bookingId = bRepo.findById(Id);
		Booking book = bookingId.get();
		Venue venue = book.getVenue();
		double totalPrice = priceCalc(startTime, endTime, venue);
		if (totalPrice > 0) {
			book.setStartTime(startTime);
			book.setEndTime(endTime);
			book.setTotalPrice(totalPrice);
			book.setPaymentStatus("Pending Approval");
			bRepo.save(book);
			model.addAttribute("UpdateSuccess", true);
			showVenues(model);
			myBookings(model);
			return "home";
		} else {
			model.addAttribute("UpdateFailed", true);
			showVenues(model);
			myBookings(model);
			return "home";
		}
	}

	public static double priceCalc(LocalDateTime startTime, LocalDateTime endTime, Venue venue) {
		Duration duration = Duration.between(startTime, endTime);
		long hours = duration.toHours();
		double pricePerHour = venue.getPricePerHour();
		double totalPrice = hours * pricePerHour;
		return totalPrice;
	}

	public static String strHasInt(String str) {
		if (str.matches("[a-zA-Z\\s]+"))
			return str;
		else
			return "";
	}

	public static boolean emailvalidation(String email) {
		String emailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(emailRegex);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public static boolean containsWhiteSpace(String pass) {
		String pattern = "\\s";
		Pattern whitespacePattern = Pattern.compile(pattern);
		Matcher matcher = whitespacePattern.matcher(pass);
		return matcher.find();
	}

	public void showVenues(Model model) {
		List<Venue> venues = vRepo.findAll();
		model.addAttribute("venues", venues);
	}

	public void myBookings(Model model) {
		List<User> users = repo.findAllByEmail(customerEmail);
		User user = users.get(0);
		List<Booking> booking = bRepo.findAllByUserId(user.getId());
		if (!booking.isEmpty()) {
			model.addAttribute("booking", booking);
		}
	}

	public void updateBooking(Booking book) {
		LocalDateTime date = LocalDateTime.now();
		if (date.isAfter(book.getEndTime()) || book.getPaymentStatus().equals("Rejected"))
			bRepo.deleteById(book.getId());
	}
}