package com.metrosewa.ticketservice.controller;

import com.metrosewa.ticketservice.dto.TicketRequest;
import com.metrosewa.ticketservice.dto.TicketResponse;
import com.metrosewa.ticketservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/book")
    public TicketResponse bookTicket(@RequestBody TicketRequest request) {
        return ticketService.bookTicket(request);
    }
}