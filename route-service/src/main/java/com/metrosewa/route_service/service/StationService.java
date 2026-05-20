package com.metrosewa.route_service.service;

import com.metrosewa.route_service.entity.Station;

import java.util.List;

public interface StationService {

    List<Station> getAllStations();

    List<String> getStationsByLine(Long lineId);

    Station createStation(Station station);

    Station updateStation(Long id, Station updatedStation);

    void deleteStation(Long id);
}
