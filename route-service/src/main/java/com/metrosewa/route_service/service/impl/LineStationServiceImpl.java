package com.metrosewa.route_service.service.impl;

import com.metrosewa.route_service.entity.LineStation;
import com.metrosewa.route_service.exception.ResourceNotFoundException;
import com.metrosewa.route_service.repository.LineStationRepository;
import com.metrosewa.route_service.service.LineStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service handles the mapping between metro lines and stations.
 *
 * Example:
 * Purple Line has PCMC Bhavan at station order 1.
 * Purple Line has Civil Court at station order 11.
 *
 * This mapping is important because route planning needs:
 * 1. Which station belongs to which line.
 * 2. Station order on that line.
 * 3. Distance of station from the starting point of the line.
 */
@Service
@RequiredArgsConstructor
public class LineStationServiceImpl implements LineStationService {

    private final LineStationRepository lineStationRepository;

    @Override
    public List<LineStation> getAllLineStations() {
        return lineStationRepository.findAll();
    }

    @Override
    public LineStation createLineStation(LineStation lineStation) {
        return lineStationRepository.save(lineStation);
    }

    /**
     * Updates an existing line-station mapping.
     *
     * First it checks whether mapping exists or not.
     * If mapping is not found, it throws ResourceNotFoundException.
     *
     * Only mapping fields are updated.
     * Database id is not changed.
     */
    @Override
    public LineStation updateLineStation(Long id, LineStation updatedLineStation) {
        LineStation existing = lineStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Line-station mapping not found with id: " + id
                ));

        existing.setLineId(updatedLineStation.getLineId());
        existing.setStationId(updatedLineStation.getStationId());
        existing.setStationOrder(updatedLineStation.getStationOrder());
        existing.setDistanceFromStart(updatedLineStation.getDistanceFromStart());

        return lineStationRepository.save(existing);
    }

    /**
     * Deletes a line-station mapping by id.
     *
     * Before deleting, it checks whether the mapping exists.
     * This avoids silent delete when wrong id is passed.
     */
    @Override
    public void deleteLineStation(Long id) {
        if (!lineStationRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Line-station mapping not found with id: " + id
            );
        }

        lineStationRepository.deleteById(id);
    }
}