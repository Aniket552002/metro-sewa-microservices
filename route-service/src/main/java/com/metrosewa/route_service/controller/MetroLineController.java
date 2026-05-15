package com.metrosewa.route_service.controller;

import com.metrosewa.route_service.entity.MetroLine;
import com.metrosewa.route_service.service.MetroLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lines")
@RequiredArgsConstructor
public class MetroLineController {

    private final MetroLineService metroLineService;

    @GetMapping
    public List<MetroLine> getAllLines() {
        return metroLineService.getAllLines();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetroLine createLine(@RequestBody MetroLine metroLine) {
        return metroLineService.createLine(metroLine);
    }

    @PutMapping("/{id}")
    public MetroLine updateLine(@PathVariable Long id, @RequestBody MetroLine metroLine) {
        return metroLineService.updateLine(id, metroLine);
    }

    @DeleteMapping("/{id}")
    public String deleteLine(@PathVariable Long id) {
        metroLineService.deleteLine(id);
        return "Metro line deleted successfully";
    }
}