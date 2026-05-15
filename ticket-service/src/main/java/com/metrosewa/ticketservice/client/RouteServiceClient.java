package com.metrosewa.ticketservice.client;

import com.metrosewa.ticketservice.dto.RouteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "route-service",
        url = "${route-service.url}"
)
public interface RouteServiceClient {

    @GetMapping("/api/route-planner/plan")
    RouteResponse planRoute(
            @RequestParam("source") String source,
            @RequestParam("destination") String destination
    );
}