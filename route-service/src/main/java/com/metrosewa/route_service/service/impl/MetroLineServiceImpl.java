package com.metrosewa.route_service.service.impl;

import com.metrosewa.route_service.entity.MetroLine;
import com.metrosewa.route_service.repository.MetroLineRepository;
import com.metrosewa.route_service.service.MetroLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
/**
 * This service handles metro line master data.
 *
 * It is used to create, update, delete, and fetch metro lines like Purple Line,
 * Aqua Line, or Red Line. Only active metro lines are returned to users.
 */
@Service
@RequiredArgsConstructor
public class MetroLineServiceImpl implements MetroLineService {

    private final MetroLineRepository metroLineRepository;

    @Override
    public List<MetroLine> getAllLines() {
        // Only active metro lines are shown to users.
        return metroLineRepository.findByActiveTrue();
    }

    @Override
    public MetroLine createLine(MetroLine metroLine) {
        return metroLineRepository.save(metroLine);
    }

    @Override
    public MetroLine updateLine(Long id, MetroLine updatedLine) {
        // Update the existing line instead of creating a new row.
        MetroLine existingLine = metroLineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Metro line not found"
                ));

        existingLine.setLineNumber(updatedLine.getLineNumber());
        existingLine.setLineName(updatedLine.getLineName());
        existingLine.setColorCode(updatedLine.getColorCode());
        existingLine.setStartStation(updatedLine.getStartStation());
        existingLine.setEndStation(updatedLine.getEndStation());
        existingLine.setActive(updatedLine.getActive());

        return metroLineRepository.save(existingLine);
    }

    @Override
    public void deleteLine(Long id) {
        metroLineRepository.deleteById(id);
    }
}
