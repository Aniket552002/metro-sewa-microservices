package com.metrosewa.route_service.repository;

import com.metrosewa.route_service.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
//repository for managing station master data and finding stations by name for route planning.
public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByStationNameIgnoreCase(String stationName);
}