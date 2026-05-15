package com.metrosewa.route_service.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RoutePlanResponse {

    private String source;

    private String destination;

    private Integer totalStations;

    private Double totalDistance;

    private Integer estimatedTimeMinutes;

    private Double fare;

    private Integer interchanges;

    private List<RouteSegment> routeSegments;
}