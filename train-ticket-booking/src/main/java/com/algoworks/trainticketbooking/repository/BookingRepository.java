package com.algoworks.trainticketbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algoworks.trainticketbooking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long>{

	Optional<Booking> findByPnr (String pnr); //find one specific booking using pnr
	
	List<Booking> findByUserId(Long userId); //find all booking made by particular user
}
