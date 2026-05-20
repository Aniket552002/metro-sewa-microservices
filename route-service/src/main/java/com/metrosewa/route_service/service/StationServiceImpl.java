package com.metrosewa.route_service.service;

import com.metrosewa.route_service.entity.LineStation;
import com.metrosewa.route_service.entity.Station;
import com.metrosewa.route_service.repository.LineStationRepository;
import com.metrosewa.route_service.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final LineStationRepository lineStationRepository;

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public List<String> getStationsByLine(Long lineId) {
        List<LineStation> lineStations =
                lineStationRepository.findByLineIdOrderByStationOrderAsc(lineId);

        List<String> stationNames = new ArrayList<>();

        for (LineStation lineStation : lineStations) {
            Station station = stationRepository.findById(lineStation.getStationId())
                    .orElseThrow(() -> new RuntimeException("Station not found"));

            stationNames.add(station.getStationName());
        }

        return stationNames;
    }

    @Override
    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    @Override
    public Station updateStation(Long id, Station updatedStation) {
        Station existingStation = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        existingStation.setStationName(updatedStation.getStationName());
        existingStation.setStationCode(updatedStation.getStationCode());
        existingStation.setInterchange(updatedStation.getInterchange());
        existingStation.setActive(updatedStation.getActive());

        return stationRepository.save(existingStation);
    }

    @Override
    public void deleteStation(Long id) {
        stationRepository.deleteById(id);
    }
}
