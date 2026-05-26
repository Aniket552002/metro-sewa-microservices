package com.metrosewa.route_service.service.impl;

import com.metrosewa.route_service.document.StationDocument;
import com.metrosewa.route_service.entity.Station;
import com.metrosewa.route_service.repository.StationRepository;
import com.metrosewa.route_service.repository.StationSearchRepository;
import com.metrosewa.route_service.service.StationSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * This service handles station search using Elasticsearch.
 *
 * MariaDB is the main source of station data, but Elasticsearch stores a searchable
 * copy of stations for faster search. syncStationsToElastic() copies station data
 * from MariaDB to Elasticsearch, and searchStations() searches stations by name.
 */
@Service
@RequiredArgsConstructor
public class StationSearchServiceImpl implements StationSearchService {

    private final StationRepository stationRepository;

    private final StationSearchRepository stationSearchRepository;

    @Override
    public String syncStationsToElastic() {
        // Elasticsearch needs a separate document copy of station data for quick searching.
        List<Station> stations = stationRepository.findAll();

        List<StationDocument> documents = stations.stream()
                .map(station -> StationDocument.builder()
                        .id(station.getId().toString())
                        .stationName(station.getStationName())
                        .stationCode(station.getStationCode())
                        .build())
                .toList();

        stationSearchRepository.saveAll(documents);

        return "Stations synced to Elasticsearch";
    }

    @Override
    public List<StationDocument> searchStations(String query) {
        // Search is based on station name so users can type partial station names.
        return stationSearchRepository
                .findByStationNameContainingIgnoreCase(query);
    }
}
