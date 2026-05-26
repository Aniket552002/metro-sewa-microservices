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

    @PostMapping("/sync")
    public String syncStations() {
        // Copies station data from MariaDB to Elasticsearch for search.
        return stationSearchService.syncStationsToElastic();
    }

    @GetMapping("/stations")
    public List<StationDocument> searchStations(@RequestParam String query) {
        // Searches station names using Elasticsearch.
        return stationSearchService.searchStations(query);
    }
}
