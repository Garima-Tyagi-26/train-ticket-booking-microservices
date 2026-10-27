package com.algoworks.trainticketbooking.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algoworks.trainticketbooking.repository.BookingRepository;
import com.algoworks.trainticketbooking.repository.TrainRepository;
import com.algoworks.trainticketbooking.service.MemcachedService;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import com.algoworks.trainticketbooking.dto.BookingRequest;
import com.algoworks.trainticketbooking.dto.BookingResponse;
import com.algoworks.trainticketbooking.dto.PassengerRequest;
import com.algoworks.trainticketbooking.entity.Booking;
import com.algoworks.trainticketbooking.entity.Train;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TrainRepository trainRepository;

    @Mock
    private MemcachedService memcachedService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BookingService bookingService;
    
    
    // helper methods
     private Authentication createAuthentication() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("userId", 101L)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);

        return auth;
    }
    
    private Train createTrain(Long trainId, int availableSeats) {
    	
        Train train = new Train();
        train.setId(trainId);
        train.setFare(new BigDecimal("500.00"));
        train.setAvailableSeats(availableSeats);
        return train;
    }

    private PassengerRequest createPassenger(String name, int age, String gender) {

        PassengerRequest passenger = new PassengerRequest();
        passenger.setName(name);
        passenger.setAge(age);
        passenger.setGender(gender);
        return passenger;
    }
    
    
    @Test
    void createBooking_validRequest_shouldReturnConfirmedBooking() {
    	// Valid request - Booking confirmed

      // Arrange
      Long userId = 101L;
      Long trainId = 1L;

      Train train = createTrain(trainId, 10);

      PassengerRequest passenger1 = createPassenger("Rahul", 25, "Male");

      PassengerRequest passenger2 = createPassenger("Priya", 23, "Female");

      BookingRequest request = new BookingRequest();
      request.setTrainId(trainId);
      request.setPassengers(List.of(passenger1, passenger2));

      when(trainRepository.findById(trainId)).thenReturn(Optional.of(train));

      when(trainRepository.reduceSeatsIfAvailable(trainId, 2)).thenReturn(1);

      when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      Authentication authentication = createAuthentication();

      BookingResponse response = bookingService.createBooking(request, authentication);

      // Assert
      assertNotNull(response);
      assertEquals("CONFIRMED", response.getStatus());
      assertEquals(2, response.getNumberOfSeats());
      assertEquals(new BigDecimal("1000.00"), response.getTotalFare());
      assertNotNull(response.getPnr());
      assertEquals(8, response.getPnr().length());
      assertEquals(2, response.getPassengers().size());

      verify(trainRepository).findById(trainId);
      verify(trainRepository).reduceSeatsIfAvailable(trainId, 2);

      // Verify fields on the saved entity
      verify(bookingRepository).save(argThat(booking ->
            booking.getUserId().equals(userId)
            && booking.getTrain().getId().equals(trainId)
            && booking.getNumberOfSeats() == 2
            && booking.getPassengers().size() == 2));
   }
    
    
     @Test
     void createBooking_trainNotFound_shouldThrowNotFoundException() {

       // Arrange
       Long trainId = 99L;

       PassengerRequest passenger = createPassenger("Rahul", 25, "Male");

       BookingRequest request = new BookingRequest();
       request.setTrainId(trainId);
       request.setPassengers(List.of(passenger));
 
       when(trainRepository.findById(trainId)).thenReturn(Optional.empty());

       // Act
       Authentication authentication = createAuthentication();
       
       ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                          () -> bookingService.createBooking(request, authentication));

       // Assert
       assertEquals(404, exception.getStatusCode().value());

       verify(trainRepository).findById(trainId);
       verify(trainRepository, never()).reduceSeatsIfAvailable(anyLong(), anyInt());
       verify(bookingRepository, never()).save(any(Booking.class));
    }
     
     
    @Test
    void createBooking_nullPassengers_shouldThrowBadRequest() {
    	//test to check service rejects a request with no passenger list

      // Arrange
      Long trainId = 1L;

      Train train = createTrain(trainId, 10);
      
      BookingRequest request = new BookingRequest();
      request.setTrainId(trainId);
      request.setPassengers(null);

      when(trainRepository.findById(trainId)).thenReturn(Optional.of(train));

      // Act
      Authentication authentication = createAuthentication();
      
      ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> bookingService.createBooking(request, authentication));

      // Assert
      assertEquals(400, exception.getStatusCode().value());

      verify(trainRepository).findById(trainId);
      verify(trainRepository, never()).reduceSeatsIfAvailable(anyLong(), anyInt());
      verify(bookingRepository, never()).save(any(Booking.class));
      
    }


    @Test
    void createBooking_insufficientSeats_shouldThrowConflict() {
    	//testing the scenario of insufficient seats

        // Arrange
        Long trainId = 1L;

        Train train = createTrain(trainId, 10);

        PassengerRequest passenger1 = createPassenger("Rahul", 25, "Male");

        PassengerRequest passenger2 = createPassenger("Priya", 23, "Female");

        BookingRequest request = new BookingRequest();
        request.setTrainId(trainId);
        request.setPassengers(List.of(passenger1, passenger2));

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(train));

        when(trainRepository.reduceSeatsIfAvailable(trainId, 2)).thenReturn(0);

        // Act
        Authentication authentication = createAuthentication();
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking(request, authentication));

        // Assert
        assertEquals(409, exception.getStatusCode().value());

        verify(trainRepository).findById(trainId);
        verify(trainRepository).reduceSeatsIfAvailable(trainId, 2);
        verify(bookingRepository, never()).save(any(Booking.class));
    }
    

    @Test
    void createBooking_missingAuthentication_shouldThrowAccessDenied() {

        // Arrange
        BookingRequest request = new BookingRequest();
        request.setTrainId(1L);
        request.setPassengers(List.of());

        // Act and Assert
        assertThrows(AccessDeniedException.class,() -> bookingService.createBooking(request, null));

        // checking no repository calls are made
        verifyNoInteractions(trainRepository, bookingRepository);
    }
 
}

