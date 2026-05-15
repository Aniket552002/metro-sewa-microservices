package com.metrosewa.ticketservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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