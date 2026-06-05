package com.metrosewa.route_service.service.impl;

import com.metrosewa.route_service.entity.Station;
import com.metrosewa.route_service.repository.LineStationRepository;
import com.metrosewa.route_service.repository.StationRepository;
import com.metrosewa.route_service.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * This service handles station master data.
 *
 * It is used to create, update, delete, and fetch metro stations.
 * It also gives stations for a specific metro line in correct travel order
 * using the line-station mapping table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final LineStationRepository lineStationRepository;

    @Override
    public List<Station> getAllStations() {
        log.info("Fetching all stations from database");
        return stationRepository.findAll();
    }

    /**
     * Redis cache is used here because stations by line are requested repeatedly.
     *
     * First request:
     * - Data comes from MariaDB using a single JOIN query
     * - Result is stored in Redis cache
     *
     * Second request with same lineId:
     * - Data comes directly from Redis
     * - Database query is skipped
     *
     * Scalability improvement:
     * Earlier this method had an N+1 query problem:
     * - 1 query to fetch line-station mappings
     * - then 1 extra query per station inside the loop
     *
     * Now it uses one JOIN query through findStationNamesByLineId().
     * This reduces database calls and makes the API better for high traffic.
     */
    @Override
    @Cacheable(value = "stationsByLine", key = "#lineId")
    public List<String> getStationsByLine(Long lineId) {

        log.info("Fetching stations from database for lineId: {}", lineId);

        List<String> stationNames = lineStationRepository.findStationNamesByLineId(lineId);

        if (stationNames.isEmpty()) {
            log.warn("No stations found for lineId: {}", lineId);

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No stations found for lineId: " + lineId
            );
        }

        return stationNames;
    }

    /**
     * When station is created, station cache is cleared.
     */
    @Override
    @CacheEvict(value = {"routePlans", "stationsByLine"}, allEntries = true)
    public Station createStation(Station station) {
        log.info("Creating new station: {}", station.getStationName());
        return stationRepository.save(station);
    }

    /**
     * When station is updated, station cache is cleared.
     */
    @Override
    @CacheEvict(value = {"routePlans", "stationsByLine"}, allEntries = true)
    public Station updateStation(Long id, Station updatedStation) {

        log.info("Updating station with id: {}", id);

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
    @CacheEvict(value = {"routePlans", "stationsByLine"}, allEntries = true)
    public void deleteStation(Long id) {
        log.info("Deleting station with id: {}", id);
        stationRepository.deleteById(id);
    }
}