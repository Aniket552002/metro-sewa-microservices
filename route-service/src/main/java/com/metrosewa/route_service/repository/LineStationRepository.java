package com.metrosewa.route_service.repository;

import com.metrosewa.route_service.entity.LineStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Repository for managing metro line-station mappings used in station ordering and route planning.
public interface LineStationRepository extends JpaRepository<LineStation, Long> {

    // Fetches all line-station mappings for a line in station order.
    // This returns LineStation entities, so use only when complete mapping details are required.
    List<LineStation> findByLineIdOrderByStationOrderAsc(Long lineId);

    // Finds a specific station mapping inside a specific metro line.
    // Useful for validation when checking if a station belongs to a line.
    Optional<LineStation> findByLineIdAndStationId(Long lineId, Long stationId);

    // Finds all line mappings for a station.
    // Useful for checking if a station is part of multiple lines, like an interchange station.
    List<LineStation> findByStationId(Long stationId);

    // Fetches all station names for a line using a single JOIN query.
    // This avoids the N+1 query problem in StationServiceImpl.getStationsByLine().
    @Query(
            value = """
                    SELECT s.station_name
                    FROM line_stations ls
                    JOIN stations s
                    ON ls.station_id = s.id
                    WHERE ls.line_id = :lineId
                    ORDER BY ls.station_order ASC
                    """,
            nativeQuery = true
    )
    List<String> findStationNamesByLineId(@Param("lineId") Long lineId);

    // Fetches station names between two station orders using a single JOIN query.
    // Used while preparing a route segment between source, destination, or interchange station.
    @Query(
            value = """
                    SELECT s.station_name
                    FROM line_stations ls
                    JOIN stations s
                    ON ls.station_id = s.id
                    WHERE ls.line_id = :lineId
                    AND ls.station_order BETWEEN :start AND :end
                    ORDER BY ls.station_order ASC
                    """,
            nativeQuery = true
    )
    List<String> findStationNamesBetweenOrders(
            @Param("lineId") Long lineId,
            @Param("start") int start,
            @Param("end") int end
    );
}