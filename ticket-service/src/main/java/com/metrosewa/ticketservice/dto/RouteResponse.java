package com.metrosewa.ticketservice.dto;

import lombok.Data;

@Data
public class RouteResponse {

    private String source;
    private String destination;
    private Integer totalStations;
    private Double totalDistance;
    private Integer estimatedTimeMinutes;
    private Double fare;
    private Integer interchanges;
}