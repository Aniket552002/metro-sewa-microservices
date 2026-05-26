package com.metrosewa.route_service.repository;

import com.metrosewa.route_service.entity.LineStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Repository for managing metro line-station mappings used in station ordering and route planning.
public interface LineStationRepository extends JpaRepository<LineStation, Long> {

    List<LineStation> findByLineIdOrderByStationOrderAsc(Long lineId);

    Optional<LineStation> findByLineIdAndStationId(Long lineId, Long stationId);

    List<LineStation> findByStationId(Long stationId);

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