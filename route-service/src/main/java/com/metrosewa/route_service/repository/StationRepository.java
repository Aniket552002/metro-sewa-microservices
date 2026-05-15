package com.metrosewa.route_service.repository;

import com.metrosewa.route_service.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByStationNameIgnoreCase(String stationName);
}