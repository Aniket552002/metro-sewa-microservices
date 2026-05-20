package com.metrosewa.route_service.service;

import com.metrosewa.route_service.entity.MetroLine;
import com.metrosewa.route_service.repository.MetroLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetroLineServiceImpl implements MetroLineService {

    private final MetroLineRepository metroLineRepository;

    @Override
    public List<MetroLine> getAllLines() {
        return metroLineRepository.findByActiveTrue();
    }

    @Override
    public MetroLine createLine(MetroLine metroLine) {
        return metroLineRepository.save(metroLine);
    }

    @Override
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

    @Override
    public void deleteLine(Long id) {
        metroLineRepository.deleteById(id);
    }
}
