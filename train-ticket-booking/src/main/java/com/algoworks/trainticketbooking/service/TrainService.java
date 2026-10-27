package com.algoworks.trainticketbooking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.algoworks.trainticketbooking.entity.Train;
import com.algoworks.trainticketbooking.repository.TrainRepository;

@Service
public class TrainService {

	private final TrainRepository trainRepository;

	public TrainService(TrainRepository trainRepository) {
		this.trainRepository = trainRepository;
	}
	
	
	// add the train
	public Train addTrain(Train train) {
		return trainRepository.save(train);
	}
	
	
	// see all the trains
	public List<Train> getAllTrains(){
		return trainRepository.findAll();
	}
	
	
	// get train by ID
	public Train getTrainById(Long id) {
		return trainRepository.findById(id).orElse(null);
	}
	
	
	// update the existing train
	public Train updateTrain(Long id, Train train) {

	    Train existingTrain = trainRepository.findById(id).orElse(null);

	    if (existingTrain == null) {
	        return null;
	    }

	    existingTrain.setTrainName(train.getTrainName());
	    existingTrain.setTrainNumber(train.getTrainNumber());
	    existingTrain.setSource(train.getSource());
	    existingTrain.setDestination(train.getDestination());
	    existingTrain.setJourneyDate(train.getJourneyDate());
	    existingTrain.setDepartureTime(train.getDepartureTime());
	    existingTrain.setArrivalTime(train.getArrivalTime());
	    existingTrain.setTotalSeats(train.getTotalSeats());
	    existingTrain.setAvailableSeats(train.getAvailableSeats());
	    existingTrain.setFare(train.getFare());

	    return trainRepository.save(existingTrain);
	}
	
	
	// delete the train
	public void deleteTrain(Long id) {
		trainRepository.deleteById(id);
	}
	
	
	// search the train by source, destination, journeyDate
	public List<Train> searchTrains(
	        String source,
	        String destination,
	        LocalDate journeyDate) {

	    return trainRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndJourneyDate(
	                                                       source, destination, journeyDate);
	}
	
}
