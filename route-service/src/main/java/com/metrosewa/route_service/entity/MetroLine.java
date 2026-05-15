package com.metrosewa.route_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metro_lines")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MetroLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lineNumber;

    private String lineName;

    private String colorCode;

    private String startStation;

    private String endStation;

    private Boolean active;
}