package com.algoworks.trainticketbooking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.algoworks.trainticketbooking.dto.BookingRequest;
import com.algoworks.trainticketbooking.dto.BookingResponse;
import com.algoworks.trainticketbooking.dto.PassengerRequest;
import com.algoworks.trainticketbooking.dto.PassengerResponse;
import com.algoworks.trainticketbooking.entity.Booking;
import com.algoworks.trainticketbooking.entity.Passenger;
import com.algoworks.trainticketbooking.entity.Train;
import com.algoworks.trainticketbooking.repository.BookingRepository;
import com.algoworks.trainticketbooking.repository.TrainRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class BookingService {

    private final ObjectMapper objectMapper;
    private final MemcachedService memcachedService;
    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;

    
    public BookingService(BookingRepository bookingRepository, TrainRepository trainRepository,
                          MemcachedService memcachedService, ObjectMapper objectMapper) {

        this.bookingRepository = bookingRepository;
        this.trainRepository = trainRepository;
        this.memcachedService = memcachedService;
        this.objectMapper = objectMapper;
    }

    
    // Get the user ID from the JWT already validated by Spring Security
    private Long getAuthenticatedUserId(Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Invalid authentication");
        }

        Object claim = jwt.getClaim("userId");

        if (!(claim instanceof Number number)) {
            throw new AccessDeniedException("User ID is missing from the access token");
        }

        return number.longValue();
    }

    
    // Create a booking for the authenticated user
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        Train train = trainRepository.findById(request.getTrainId())
                      .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Train not found"));

        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one passenger is required");
        }

        int numberOfPassengers = request.getPassengers().size();

        int updatedRows = trainRepository.reduceSeatsIfAvailable(request.getTrainId(), numberOfPassengers);

        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough seats available");
        }

        Booking booking = new Booking();

        booking.setUserId(userId);
        booking.setTrain(train);
        booking.setNumberOfSeats(numberOfPassengers);

        BigDecimal totalFare = train.getFare().multiply(BigDecimal.valueOf(numberOfPassengers));

        booking.setTotalFare(totalFare);

        booking.setPnr(UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase());

        booking.setStatus("CONFIRMED");
        booking.setBookingDate(LocalDate.now());

        List<Passenger> passengers = new ArrayList<>();

        for (PassengerRequest passengerRequest : request.getPassengers()) {
            Passenger passenger = new Passenger();

            passenger.setName(passengerRequest.getName());
            passenger.setAge(passengerRequest.getAge());
            passenger.setGender(passengerRequest.getGender());
            passenger.setBooking(booking);

            passengers.add(passenger);
        }

        booking.setPassengers(passengers);

        Booking savedBooking = bookingRepository.save(booking);

        return toBookingResponse(savedBooking);
    }

    
    // Convert Booking entity to response DTO
    private BookingResponse toBookingResponse(Booking booking) {

        BookingResponse response = new BookingResponse();

        response.setPnr(booking.getPnr());
        response.setNumberOfSeats(booking.getNumberOfSeats());
        response.setTotalFare(booking.getTotalFare());
        response.setRefundAmount(booking.getRefundAmount());
        response.setStatus(booking.getStatus());
        response.setBookingDate(booking.getBookingDate());
        response.setTrainId(booking.getTrain().getId());

        List<PassengerResponse> passengerResponses = new ArrayList<>();

        for (Passenger passenger : booking.getPassengers()) {
            PassengerResponse passengerResponse = new PassengerResponse();

            passengerResponse.setName(passenger.getName());
            passengerResponse.setAge(passenger.getAge());
            passengerResponse.setGender(passenger.getGender());

            passengerResponses.add(passengerResponse);
        }

        response.setPassengers(passengerResponses);

        return response;
    }

    
    // Find booking using PNR, with Mem-cached
    public BookingResponse getBookingByPnr(String pnr) {

        String key = "booking:" + pnr;

        String cachedBooking = memcachedService.get(key);

        if (cachedBooking != null) {
            try {
                return objectMapper.readValue(
                        cachedBooking, BookingResponse.class);
            } catch (Exception e) {
                throw new RuntimeException("Error reading booking from cache", e);
            }
        }

        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        BookingResponse response = toBookingResponse(booking);

        try {
            String json = objectMapper.writeValueAsString(response);
            memcachedService.set(key, json, 300);
        } catch (Exception e) {
            throw new RuntimeException("Error saving booking to cache", e);
        }

        return response;
    }

    
    // Get only the authenticated user's booking history
    public List<Booking> getBookingHistory(Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        return bookingRepository.findByUserId(userId);
    }

    
    // Get a booking only if it belongs to the authenticated user
    public Booking getBooking(Long bookingId, Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (!userId.equals(booking.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        return booking;
    }

    
    // Cancel a booking and restore its available seats
    @Transactional
    public Booking cancelBooking(Long bookingId, Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (!userId.equals(booking.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is already cancelled");
        }

        Train train = booking.getTrain();

        train.setAvailableSeats(train.getAvailableSeats() + booking.getNumberOfSeats());

        booking.setStatus("CANCELLED");
        booking.setRefundAmount(booking.getTotalFare());

        trainRepository.save(train);

        return bookingRepository.save(booking);
    }
    
}


