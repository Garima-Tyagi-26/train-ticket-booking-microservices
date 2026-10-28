package com.algoworks.trainticketbooking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algoworks.trainticketbooking.entity.SeatReservation;
import com.algoworks.trainticketbooking.entity.Train;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
	
	boolean existsByTrainAndSeatNumber(Train train, Integer seatNumber);
	// checks whether a reservation exists for that train and seat number.
	
	List<SeatReservation> findByTrain(Train train);
	// Returns the reservation records associated with a train
	
	List<SeatReservation> findByTrainAndStatus(Train train, String status);
	// retrieves all seat reservations for a particular train whose status matches the given value.

	List<SeatReservation> findByTrainAndStatusAndHoldExpiresAtAfter(Train train, String status, LocalDateTime now);
    // retrieves reservations for that train with the specified status and an expire time later than the current time.
}
