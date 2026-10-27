package com.algoworks.trainticketbooking.dto;

import java.util.List;

public class BookingRequest {

	private Long trainId;
	private List<PassengerRequest> passengers;

	public BookingRequest () {}

	public Long getTrainId() {
		return trainId;
	}

	public void setTrainId(Long trainId) {
		this.trainId = trainId;
	}

	public List<PassengerRequest> getPassengers() {
		return passengers;
	}

	public void setPassengers(List<PassengerRequest> passengers) {
		this.passengers = passengers;
	}

	
}
