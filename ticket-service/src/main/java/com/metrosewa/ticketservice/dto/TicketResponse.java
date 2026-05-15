package com.metrosewa.ticketservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketResponse {

    private Long ticketId;
    private Long userId;
    private String sourceStation;
    private String destinationStation;
    private Double fare;
    private Integer travelTime;
    private Integer totalStations;
    private Double totalDistance;
    private Integer interchanges;
    private String status;
}