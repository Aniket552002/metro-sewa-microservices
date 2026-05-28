package com.metrosewa.route_service.service.impl;

import com.metrosewa.route_service.entity.LineStation;
import com.metrosewa.route_service.entity.Station;
import com.metrosewa.route_service.repository.LineStationRepository;
import com.metrosewa.route_service.repository.StationRepository;
import com.metrosewa.route_service.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * This service handles station master data.
 *
 * It is used to create, update, delete, and fetch metro stations.
 * It also gives stations for a specific metro line in correct travel order
 * using the line-station mapping table.
 */
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final LineStationRepository lineStationRepository;

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    /**
     * Redis cache is used here because stations by line are requested repeatedly.
     *
     * First request:
     * - Data comes from MariaDB
     * - Result is stored in Redis cache
     *
     * Second request with same lineId:
     * - Data comes directly from Redis
     * - Database query is skipped
     */
    @Override
    @Cacheable(value = "stationsByLine", key = "#lineId")
    public List<String> getStationsByLine(Long lineId) {

        System.out.println("Fetching stations from database for lineId: " + lineId);

        List<LineStation> lineStations =
                lineStationRepository.findByLineIdOrderByStationOrderAsc(lineId);

        List<String> stationNames = new ArrayList<>();

        for (LineStation lineStation : lineStations) {
            Station station = stationRepository.findById(lineStation.getStationId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Station not found"
                    ));

            stationNames.add(station.getStationName());
        }

        return stationNames;
    }

    /**
     * When station is created, station cache is cleared.
     */
    @Override
    @CacheEvict(value = "stationsByLine", allEntries = true)
    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    /**
     * When station is updated, station cache is cleared.
     */
    @Override
    @CacheEvict(value = "stationsByLine", allEntries = true)
    public Station updateStation(Long id, Station updatedStation) {

        Station existingStation = stationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Station not found"
                ));

        existingStation.setStationName(updatedStation.getStationName());
        existingStation.setStationCode(updatedStation.getStationCode());
        existingStation.setInterchange(updatedStation.getInterchange());
        existingStation.setActive(updatedStation.getActive());

        return stationRepository.save(existingStation);
    }

    /**
     * When station is deleted, station cache is cleared.
     */
    @Override
    @CacheEvict(value = "stationsByLine", allEntries = true)
    public void deleteStation(Long id) {
        stationRepository.deleteById(id);
    }
}