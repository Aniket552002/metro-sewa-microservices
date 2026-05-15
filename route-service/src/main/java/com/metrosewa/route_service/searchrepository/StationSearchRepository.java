package com.metrosewa.route_service.searchrepository;

import com.metrosewa.route_service.document.StationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface StationSearchRepository
        extends ElasticsearchRepository<StationDocument, String> {

    List<StationDocument> findByStationNameContainingIgnoreCase(String stationName);
}