package com.metrosewa.ticketservice.service;

import com.metrosewa.ticketservice.dto.TicketRequest;
import com.metrosewa.ticketservice.dto.TicketResponse;

public interface TicketService {

    TicketResponse bookTicket(TicketRequest request);
}
