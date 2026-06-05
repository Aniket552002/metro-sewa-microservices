package com.metrosewa.route_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "stations",
        indexes = {
                @Index(name = "idx_station_name", columnList = "station_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "interchange")
    private Boolean interchange;

    @Column(name = "active")
    private Boolean active;
}