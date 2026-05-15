package com.metrosewa.ticketservice.dto;

import lombok.Data;

@Data
public class TicketRequest {

    private Long userId;
    private String sourceStation;
    private String destinationStation;
}