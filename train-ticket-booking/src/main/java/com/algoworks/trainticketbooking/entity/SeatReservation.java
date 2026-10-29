package com.algoworks.trainticketbooking.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "seat_reservations",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_train_seats",
				columnNames = {"train_id", "seat_number"}     // tells the database that the combination of train_id and seat_number must be unique.
		)
	)

public class SeatReservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	// many seat reservations can belong to one train and Hi-bernate generally loads the Train object when it is accessed rather than immediately loading it with every reservation.
	@ManyToOne(fetch = FetchType.LAZY)   
	// specifies the foreign-key column and the database column must not be NULL.
	@JoinColumn(name = "train_id", nullable = false)  
	private Train train;
	
	// userId identifies who created the hold.
	@Column(name = "user_id", nullable = false)
	private Long userId;
	
	@Column(name = "seat_number", nullable = false)
	private Integer seatNumber;
	
	@Column(nullable = false)
	private String status;
	
    private LocalDateTime holdExpiresAt;

	public SeatReservation() {}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Train getTrain() {
		return train;
	}

	public void setTrain(Train train) {
		this.train = train;
	}

	public Integer getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(Integer seatNumber) {
		this.seatNumber = seatNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getHoldExpiresAt() {
		return holdExpiresAt;
	}

	public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
		this.holdExpiresAt = holdExpiresAt;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
}
