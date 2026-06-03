package com.metrosewa.route_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity@Table(
        name = "line_stations",
        indexes = {
                @Index(name = "idx_line_station_line_id", columnList = "lineId"),
                @Index(name = "idx_line_station_station_id", columnList = "stationId"),
                @Index(name = "idx_line_station_line_order", columnList = "lineId, stationOrder")
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

    private Long lineId;
    private Long stationId;
    private Integer stationOrder;
    private Double distanceFromStart;
}
