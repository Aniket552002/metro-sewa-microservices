package com.metrosewa.route_service.controller;

import com.metrosewa.route_service.dto.RoutePlanResponse;
import com.metrosewa.route_service.service.RoutePlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route-planner")
@RequiredArgsConstructor
public class RoutePlannerController {

    private final RoutePlannerService routePlannerService;

    @GetMapping("/plan")
    public RoutePlanResponse planRoute(
            @RequestParam String source,
            @RequestParam String destination
    ) {
        return routePlannerService.planRoute(source, destination);
    }
}