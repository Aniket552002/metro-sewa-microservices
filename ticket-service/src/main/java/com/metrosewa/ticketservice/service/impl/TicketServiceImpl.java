package com.metrosewa.ticketservice.service.impl;

import com.metrosewa.ticketservice.client.RouteServiceClient;
import com.metrosewa.ticketservice.dto.RouteResponse;
import com.metrosewa.ticketservice.dto.TicketRequest;
import com.metrosewa.ticketservice.dto.TicketResponse;
import com.metrosewa.ticketservice.entity.Ticket;
import com.metrosewa.ticketservice.repository.TicketRepository;
import com.metrosewa.ticketservice.service.TicketService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



/**
 * This service handles ticket booking logic.
 *
 * It calls route-service to get route details like fare, distance, time,
 * total stations, and interchanges. If route-service is available, ticket is saved
 * with BOOKED status. If route-service is down or fails, circuit breaker fallback
 * returns ROUTE_SERVICE_UNAVAILABLE and ticket is not saved.
 */
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final RouteServiceClient routeServiceClient;

    @Override
    @CircuitBreaker(
            name = "routeServiceCircuitBreaker",
            fallbackMethod = "routeServiceFallback"
    )
    public TicketResponse bookTicket(TicketRequest request) {

        // Ticket-service depends on route-service for fare and travel details.
        RouteResponse route = routeServiceClient.planRoute(
                request.getSourceStation(),
                request.getDestinationStation()
        );

        // Save the confirmed ticket using the route details received from route-service.
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

        return mapToTicketResponse(saved);
    }

    public TicketResponse routeServiceFallback(
            TicketRequest request,
            Throwable exception
    ) {
        // Ticket is not saved because route details and fare are unavailable.
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

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUserId())
                .sourceStation(ticket.getSourceStation())
                .destinationStation(ticket.getDestinationStation())
                .fare(ticket.getFare())
                .travelTime(ticket.getTravelTime())
                .totalStations(ticket.getTotalStations())
                .totalDistance(ticket.getTotalDistance())
                .interchanges(ticket.getInterchanges())
                .status(ticket.getStatus())
                .build();
    }
}
