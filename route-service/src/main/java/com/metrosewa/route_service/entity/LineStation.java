package com.metrosewa.route_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "line_stations")
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
