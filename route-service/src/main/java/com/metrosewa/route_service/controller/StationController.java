package com.metrosewa.route_service.controller;

import com.metrosewa.route_service.entity.Station;
import com.metrosewa.route_service.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/line/{lineId}")
    public List<String> getStationsByLine(@PathVariable Long lineId) {
        return stationService.getStationsByLine(lineId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Station createStation(@RequestBody Station station) {
        // Adds a metro station master record.
        return stationService.createStation(station);
    }

    @PutMapping("/{id}")
    public Station updateStation(@PathVariable Long id, @RequestBody Station station) {
        return stationService.updateStation(id, station);
    }

    @DeleteMapping("/{id}")
    public String deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return "Station deleted successfully";
    }
}
