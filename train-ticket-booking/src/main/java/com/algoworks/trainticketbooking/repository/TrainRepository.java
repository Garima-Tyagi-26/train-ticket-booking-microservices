package com.algoworks.trainticketbooking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.algoworks.trainticketbooking.entity.Train;

public interface TrainRepository extends JpaRepository<Train, Long>{

	List<Train> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndJourneyDate(
	        String source,
	        String destination,
	        LocalDate journeyDate);
	
	@Modifying                             
	@Query("""
	    UPDATE Train t
	    SET t.availableSeats = t.availableSeats - :seats
	    WHERE t.id = :trainId
	    AND t.availableSeats >= :seats """)
	// performs an atomic conditional update at the database level.
	// Seat availability update
	int reduceSeatsIfAvailable(Long trainId, int seats);
}
