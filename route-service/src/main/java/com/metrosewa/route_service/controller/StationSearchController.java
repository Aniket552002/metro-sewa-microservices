package com.metrosewa.route_service.controller;

import com.metrosewa.route_service.document.StationDocument;
import com.metrosewa.route_service.service.StationSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class StationSearchController {

    private final StationSearchService stationSearchService;

    // ================= SYNC =================

    @PostMapping("/sync")
    public String syncStations() {

        return stationSearchService.syncStationsToElastic();
    }

    // ================= SEARCH =================

    @GetMapping("/stations")
    public List<StationDocument> searchStations(
            @RequestParam String query
    ) {

        return stationSearchService.searchStations(query);
    }
}