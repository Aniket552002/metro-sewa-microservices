package com.metrosewa.route_service.service;

import com.metrosewa.route_service.entity.LineStation;

import java.util.List;

public interface LineStationService {

    List<LineStation> getAllLineStations();

    LineStation createLineStation(LineStation lineStation);

    LineStation updateLineStation(Long id, LineStation updatedLineStation);

    void deleteLineStation(Long id);
}
