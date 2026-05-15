package com.metrosewa.route_service.service;

import com.metrosewa.route_service.entity.MetroLine;
import com.metrosewa.route_service.repository.MetroLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetroLineService {

    private final MetroLineRepository metroLineRepository;

    public List<MetroLine> getAllLines() {
        return metroLineRepository.findByActiveTrue();
    }

    public MetroLine createLine(MetroLine metroLine) {
        return metroLineRepository.save(metroLine);
    }

    public MetroLine updateLine(Long id, MetroLine updatedLine) {
        MetroLine existingLine = metroLineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Metro line not found"));

        existingLine.setLineNumber(updatedLine.getLineNumber());
        existingLine.setLineName(updatedLine.getLineName());
        existingLine.setColorCode(updatedLine.getColorCode());
        existingLine.setStartStation(updatedLine.getStartStation());
        existingLine.setEndStation(updatedLine.getEndStation());
        existingLine.setActive(updatedLine.getActive());

        return metroLineRepository.save(existingLine);
    }

    public void deleteLine(Long id) {
        metroLineRepository.deleteById(id);
    }
}