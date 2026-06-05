package com.metrosewa.route_service.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePlanResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String source;
    private String destination;
    private Integer totalStations;
    private Double totalDistance;
    private Integer estimatedTimeMinutes;
    private Double fare;
    private Integer interchanges;
    private List<RouteSegment> routeSegments;
}