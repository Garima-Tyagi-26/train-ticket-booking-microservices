package com.algoworks.trainticketbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookingResponse {

	    private String pnr;
	    private Integer numberOfSeats;
	    private BigDecimal totalFare;
	    private BigDecimal refundAmount;
	    private String status;
	    private LocalDate bookingDate;
	    private Long trainId;
	    private List<PassengerResponse> passengers;

	    public BookingResponse() {}

		public String getPnr() {
			return pnr;
		}

		public void setPnr(String pnr) {
			this.pnr = pnr;
		}

		public Integer getNumberOfSeats() {
			return numberOfSeats;
		}

		public void setNumberOfSeats(Integer numberOfSeats) {
			this.numberOfSeats = numberOfSeats;
		}

		public BigDecimal getTotalFare() {
			return totalFare;
		}

		public void setTotalFare(BigDecimal totalFare) {
			this.totalFare = totalFare;
		}

		public BigDecimal getRefundAmount() {
			return refundAmount;
		}

		public void setRefundAmount(BigDecimal refundAmount) {
			this.refundAmount = refundAmount;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public LocalDate getBookingDate() {
			return bookingDate;
		}

		public void setBookingDate(LocalDate bookingDate) {
			this.bookingDate = bookingDate;
		}

		public Long getTrainId() {
			return trainId;
		}

		public void setTrainId(Long trainId) {
			this.trainId = trainId;
		}

		public List<PassengerResponse> getPassengers() {
			return passengers;
		}

		public void setPassengers(List<PassengerResponse> passengers) {
			this.passengers = passengers;
		}
	    
	    
}
