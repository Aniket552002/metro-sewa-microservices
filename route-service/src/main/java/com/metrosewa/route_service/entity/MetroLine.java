package com.metrosewa.route_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "metro_lines",
        indexes = {
                @Index(name = "idx_metro_line_number", columnList = "line_number"),
                @Index(name = "idx_metro_line_active", columnList = "active")
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

    @Column(name = "line_number", nullable = false)
    private String lineNumber;

    @Column(name = "line_name", nullable = false)
    private String lineName;

    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "active")
    private Boolean active;
}