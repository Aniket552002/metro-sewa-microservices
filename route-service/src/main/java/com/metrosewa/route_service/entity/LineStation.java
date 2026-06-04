package com.metrosewa.route_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "line_stations",
        indexes = {
                @Index(name = "idx_line_station_line_id", columnList = "line_id"),
                @Index(name = "idx_line_station_station_id", columnList = "station_id"),
                @Index(name = "idx_line_station_line_order", columnList = "line_id, station_order")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_id", nullable = false)
    private Long lineId;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "station_order", nullable = false)
    private Integer stationOrder;

    @Column(name = "distance_from_start")
    private Double distanceFromStart;
}