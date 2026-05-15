package com.metrosewa.route_service.repository;

import com.metrosewa.route_service.entity.LineStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LineStationRepository extends JpaRepository<LineStation, Long> {

    List<LineStation> findByLineIdOrderByStationOrderAsc(Long lineId);

    Optional<LineStation> findByLineIdAndStationId(Long lineId, Long stationId);

    List<LineStation> findByStationId(Long stationId);
}