package com.metrosewa.route_service.service;

import com.metrosewa.route_service.dto.RoutePlanResponse;

public interface RoutePlannerService {

    RoutePlanResponse planRoute(String sourceName, String destinationName);
}
