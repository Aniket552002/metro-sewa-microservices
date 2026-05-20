package com.metrosewa.ticketservice.service;

import com.metrosewa.ticketservice.client.RouteServiceClient;
import com.metrosewa.ticketservice.dto.RouteResponse;
import com.metrosewa.ticketservice.dto.TicketRequest;
import com.metrosewa.ticketservice.dto.TicketResponse;
import com.metrosewa.ticketservice.entity.Ticket;
import com.metrosewa.ticketservice.repository.TicketRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final RouteServiceClient routeServiceClient;

    @Override
    @CircuitBreaker(
            name = "routeService",
            fallbackMethod = "routeServiceFallback"
    )
    public TicketResponse bookTicket(TicketRequest request) {

        RouteResponse route = routeServiceClient.planRoute(
                request.getSourceStation(),
                request.getDestinationStation()
        );

        Ticket ticket = new Ticket();
        ticket.setUserId(request.getUserId());
        ticket.setSourceStation(route.getSource());
        ticket.setDestinationStation(route.getDestination());
        ticket.setFare(route.getFare());
        ticket.setTravelTime(route.getEstimatedTimeMinutes());
        ticket.setTotalStations(route.getTotalStations());
        ticket.setTotalDistance(route.getTotalDistance());
        ticket.setInterchanges(route.getInterchanges());
        ticket.setStatus("BOOKED");

        Ticket saved = ticketRepository.save(ticket);

        return TicketResponse.builder()
                .ticketId(saved.getTicketId())
                .userId(saved.getUserId())
                .sourceStation(saved.getSourceStation())
                .destinationStation(saved.getDestinationStation())
                .fare(saved.getFare())
                .travelTime(saved.getTravelTime())
                .totalStations(saved.getTotalStations())
                .totalDistance(saved.getTotalDistance())
                .interchanges(saved.getInterchanges())
                .status(saved.getStatus())
                .build();
    }

    public TicketResponse routeServiceFallback(
            TicketRequest request,
            Throwable exception
    ) {

        return TicketResponse.builder()
                .ticketId(null)
                .userId(request.getUserId())
                .sourceStation(request.getSourceStation())
                .destinationStation(request.getDestinationStation())
                .fare(0.0)
                .travelTime(0)
                .totalStations(0)
                .totalDistance(0.0)
                .interchanges(0)
                .status("ROUTE_SERVICE_UNAVAILABLE")
                .build();
    }
}
