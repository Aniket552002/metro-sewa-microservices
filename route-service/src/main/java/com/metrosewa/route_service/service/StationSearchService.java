package com.metrosewa.route_service.service;

import com.metrosewa.route_service.document.StationDocument;
import com.metrosewa.route_service.entity.Station;
import com.metrosewa.route_service.repository.StationRepository;
import com.metrosewa.route_service.searchrepository.StationSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationSearchService {

    private final StationRepository stationRepository;

    private final StationSearchRepository stationSearchRepository;

    // ================= SYNC DATA =================

    public String syncStationsToElastic() {

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

    // ================= SEARCH =================

    public List<StationDocument> searchStations(String query) {

        return stationSearchRepository
                .findByStationNameContainingIgnoreCase(query);
    }
}