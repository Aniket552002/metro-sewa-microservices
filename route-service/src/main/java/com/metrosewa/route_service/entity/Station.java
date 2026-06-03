package com.metrosewa.route_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "stations",
        indexes = {
                @Index(name = "idx_station_name", columnList = "stationName")
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
    private String stationName;
    private String stationCode;
    private Boolean interchange;
    private Boolean active;
}