package com.metrosewa.route_service.controller;

import com.metrosewa.route_service.entity.LineStation;
import com.metrosewa.route_service.service.LineStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/line-stations")
@RequiredArgsConstructor
public class LineStationController {

    private final LineStationService lineStationService;

    @GetMapping
    public List<LineStation> getAllLineStations() {
        return lineStationService.getAllLineStations();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LineStation createLineStation(@RequestBody LineStation lineStation) {
        // Stores which station belongs to which metro line and at what order.
        return lineStationService.createLineStation(lineStation);
    }

    @PutMapping("/{id}")
    public LineStation updateLineStation(@PathVariable Long id, @RequestBody LineStation lineStation) {
        return lineStationService.updateLineStation(id, lineStation);
    }

    @DeleteMapping("/{id}")
    public String deleteLineStation(@PathVariable Long id) {
        lineStationService.deleteLineStation(id);
        return "Line-station mapping deleted successfully";
    }
}
