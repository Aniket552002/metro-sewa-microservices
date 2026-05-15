package com.metrosewa.route_service.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RouteSegment {

    private String lineNumber;

    private String lineName;

    private String colorCode;

    private String fromStation;

    private String toStation;

    private List<String> stations;
}