package com.algoworks.trainticketbooking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.algoworks.trainticketbooking.entity.Train;
import com.algoworks.trainticketbooking.service.TrainService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/trains")
public class TrainController {

	private final TrainService trainService;

	public TrainController(TrainService trainService) {
		this.trainService = trainService;
	}
	
	
    @Operation(
		      summary = "Add a new train",
		      description = "Creates a new train. This operation is available only to ADMIN users."
    )
    @ApiResponses({
		      @ApiResponse(responseCode = "200", description = "Train created successfully"),
		      @ApiResponse(responseCode = "400", description = "Invalid train data"),
		      @ApiResponse(responseCode = "401", description = "Unauthorized"),
		      @ApiResponse(responseCode = "403", description = "Only ADMIN users can add trains")
    })
	@PostMapping
	public Train addTrain(@RequestBody Train train) {
		return trainService.addTrain(train);
	}
	
    
    @Operation(
             summary = "Get all trains",
             description = "Retrieves a list of all available trains."
    )
    @ApiResponses({
             @ApiResponse(responseCode = "200", description = "Trains retrieved successfully"),
             @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
	@GetMapping
	public List<Train> getAllTrains() {
		return trainService.getAllTrains();
	}
	
    
    @Operation(
            summary = "Get train by ID",
            description = "Retrieves train details using the train ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Train retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Train not found")
    })
	@GetMapping("/{id}")
	public Train getTrainById(@PathVariable Long id) {
		return trainService.getTrainById(id);
	}
	
    
    @Operation(
            summary = "Update a train",
            description = "Updates train details. This operation is available only to ADMIN users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Train updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid train data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN users can update trains"),
            @ApiResponse(responseCode = "404", description = "Train not found")
    })
	@PutMapping("/{id}")
	public Train updateTrain(@PathVariable Long id, @RequestBody Train train) {
		return trainService.updateTrain(id, train);
	}
	
    
    @Operation(
            summary = "Delete a train",
            description = "Deletes a train using its ID. This operation is available only to ADMIN users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Train deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN users can delete trains"),
            @ApiResponse(responseCode = "404", description = "Train not found")
    })
	@DeleteMapping("/{id}")
	public void deleteTrain(@PathVariable Long id) {
		trainService.deleteTrain(id);
	}
	
    
    @Operation(
            summary = "Search trains",
            description = "Searches for trains based on source, destination, and journey date."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trains retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
	@GetMapping("/search")
	public List<Train> searchTrain(
	        @RequestParam String source,
	        @RequestParam String destination,
	        @RequestParam LocalDate journeyDate) {

	    return trainService.searchTrains(source, destination, journeyDate);
	}
	
}
