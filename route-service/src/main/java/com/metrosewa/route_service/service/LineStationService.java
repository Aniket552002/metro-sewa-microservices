package com.metrosewa.route_service.service;

import com.metrosewa.route_service.entity.LineStation;
import com.metrosewa.route_service.repository.LineStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LineStationService {

    private final LineStationRepository lineStationRepository;

    public List<LineStation> getAllLineStations() {
        return lineStationRepository.findAll();
    }

    public LineStation createLineStation(LineStation lineStation) {
        return lineStationRepository.save(lineStation);
    }

    public LineStation updateLineStation(Long id, LineStation updatedLineStation) {
        LineStation existing = lineStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Line-station mapping not found"));

        existing.setLineId(updatedLineStation.getLineId());
        existing.setStationId(updatedLineStation.getStationId());
        existing.setStationOrder(updatedLineStation.getStationOrder());
        existing.setDistanceFromStart(updatedLineStation.getDistanceFromStart());

        return lineStationRepository.save(existing);
    }

    public void deleteLineStation(Long id) {
        lineStationRepository.deleteById(id);
    }
}