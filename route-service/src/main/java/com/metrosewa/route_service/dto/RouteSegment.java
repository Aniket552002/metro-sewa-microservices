package com.metrosewa.route_service.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteSegment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String lineNumber;
    private String lineName;
    private String colorCode;
    private String fromStation;
    private String toStation;
    private List<String> stations;
}