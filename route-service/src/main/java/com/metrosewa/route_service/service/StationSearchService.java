package com.metrosewa.route_service.service;

import com.metrosewa.route_service.document.StationDocument;

import java.util.List;

public interface StationSearchService {

    String syncStationsToElastic();

    List<StationDocument> searchStations(String query);
}
