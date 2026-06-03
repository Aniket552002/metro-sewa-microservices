package com.metrosewa.route_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "metro_lines",
        indexes = {
                @Index(name = "idx_metro_line_active", columnList = "active"),
                @Index(name = "idx_metro_line_number", columnList = "lineNumber")
        }
)
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
