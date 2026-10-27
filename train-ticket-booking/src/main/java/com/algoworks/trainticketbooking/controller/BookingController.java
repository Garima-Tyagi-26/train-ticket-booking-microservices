package com.algoworks.trainticketbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.algoworks.trainticketbooking.dto.BookingRequest;
import com.algoworks.trainticketbooking.dto.BookingResponse;
import com.algoworks.trainticketbooking.entity.Booking;
import com.algoworks.trainticketbooking.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    
    @Operation(summary = "Create a new booking")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Booking created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid booking request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Train not found")
    })
    @PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @RequestBody BookingRequest bookingRequest,
            Authentication authentication) {

        return bookingService.createBooking(bookingRequest, authentication);
    }

    
    @Operation(summary = "Get booking by PNR")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Booking found"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/pnr/{pnr}")
    public BookingResponse getBookingByPnr(@PathVariable String pnr) {
        BookingResponse response = bookingService.getBookingByPnr(pnr);

        if (response == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Booking not found");
        }

        return response;
    }

    
    @Operation(summary = "Get my booking history")
    @GetMapping
    public List<Booking> getBookingHistory(Authentication authentication) {
        return bookingService.getBookingHistory(authentication);
    }

    
    @Operation(summary = "Get booking details")
    @GetMapping("/{bookingId}")
    public Booking getBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {

        return bookingService.getBooking(bookingId, authentication);
    }

    
    @Operation(summary = "Cancel a booking")
    @PostMapping("/{bookingId}/cancel")
    public Booking cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {

        return bookingService.cancelBooking(bookingId, authentication);
    }
}

