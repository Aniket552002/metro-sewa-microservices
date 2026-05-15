package com.metrosewa.ticketservice.repository;

import com.metrosewa.ticketservice.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}