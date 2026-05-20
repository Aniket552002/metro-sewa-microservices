package com.metrosewa.route_service.service;

import com.metrosewa.route_service.dto.RoutePlanResponse;
import com.metrosewa.route_service.dto.RouteSegment;
import com.metrosewa.route_service.entity.LineStation;
import com.metrosewa.route_service.entity.MetroLine;
import com.metrosewa.route_service.entity.Station;
import com.metrosewa.route_service.repository.LineStationRepository;
import com.metrosewa.route_service.repository.MetroLineRepository;
import com.metrosewa.route_service.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoutePlannerServiceImpl implements RoutePlannerService {

    private final MetroLineRepository metroLineRepository;
    private final StationRepository stationRepository;
    private final LineStationRepository lineStationRepository;

    @Override
    public RoutePlanResponse planRoute(String sourceName, String destinationName) {

        Station source = stationRepository.findByStationNameIgnoreCase(sourceName)
                .orElseThrow(() -> new RuntimeException("Source station not found"));

        Station destination = stationRepository.findByStationNameIgnoreCase(destinationName)
                .orElseThrow(() -> new RuntimeException("Destination station not found"));

        List<LineStation> sourceLines =
                lineStationRepository.findByStationId(source.getId());

        List<LineStation> destinationLines =
                lineStationRepository.findByStationId(destination.getId());

        for (LineStation sourceLine : sourceLines) {

            for (LineStation destinationLine : destinationLines) {

                if (sourceLine.getLineId().equals(destinationLine.getLineId())) {

                    return buildDirectRoute(
                            source,
                            destination,
                            sourceLine,
                            destinationLine
                    );
                }
            }
        }

        return buildInterchangeRoute(
                source,
                destination,
                sourceLines,
                destinationLines
        );
    }

    private RoutePlanResponse buildDirectRoute(
            Station source,
            Station destination,
            LineStation sourceLineStation,
            LineStation destinationLineStation
    ) {

        MetroLine line = metroLineRepository.findById(sourceLineStation.getLineId())
                .orElseThrow(() -> new RuntimeException("Line not found"));

        List<String> stations = getStationsBetween(
                line.getId(),
                sourceLineStation.getStationOrder(),
                destinationLineStation.getStationOrder()
        );

        int totalStations =
                Math.abs(destinationLineStation.getStationOrder()
                        - sourceLineStation.getStationOrder());

        double totalDistance =
                Math.abs(destinationLineStation.getDistanceFromStart()
                        - sourceLineStation.getDistanceFromStart());

        int estimatedTime = totalStations * 3;

        double fare = calculateFare(totalDistance);

        RouteSegment segment = RouteSegment.builder()
                .lineNumber(line.getLineNumber())
                .lineName(line.getLineName())
                .colorCode(line.getColorCode())
                .fromStation(source.getStationName())
                .toStation(destination.getStationName())
                .stations(stations)
                .build();

        return RoutePlanResponse.builder()
                .source(source.getStationName())
                .destination(destination.getStationName())
                .totalStations(totalStations)
                .totalDistance(totalDistance)
                .estimatedTimeMinutes(estimatedTime)
                .fare(fare)
                .interchanges(0)
                .routeSegments(List.of(segment))
                .build();
    }

    private RoutePlanResponse buildInterchangeRoute(
            Station source,
            Station destination,
            List<LineStation> sourceLines,
            List<LineStation> destinationLines
    ) {

        Station interchange =
                stationRepository.findByStationNameIgnoreCase("Civil Court")
                        .orElseThrow(() ->
                                new RuntimeException("Interchange station not found"));

        for (LineStation sourceLine : sourceLines) {

            Optional<LineStation> sourceToInterchange =
                    lineStationRepository.findByLineIdAndStationId(
                            sourceLine.getLineId(),
                            interchange.getId()
                    );

            if (sourceToInterchange.isEmpty()) {
                continue;
            }

            for (LineStation destinationLine : destinationLines) {

                Optional<LineStation> interchangeToDestination =
                        lineStationRepository.findByLineIdAndStationId(
                                destinationLine.getLineId(),
                                interchange.getId()
                        );

                if (interchangeToDestination.isEmpty()) {
                    continue;
                }

                RoutePlanResponse firstPart =
                        buildDirectRoute(
                                source,
                                interchange,
                                sourceLine,
                                sourceToInterchange.get()
                        );

                RoutePlanResponse secondPart =
                        buildDirectRoute(
                                interchange,
                                destination,
                                interchangeToDestination.get(),
                                destinationLine
                        );

                List<RouteSegment> segments = new ArrayList<>();

                segments.addAll(firstPart.getRouteSegments());
                segments.addAll(secondPart.getRouteSegments());

                return RoutePlanResponse.builder()
                        .source(source.getStationName())
                        .destination(destination.getStationName())
                        .totalStations(
                                firstPart.getTotalStations()
                                        + secondPart.getTotalStations()
                        )
                        .totalDistance(
                                firstPart.getTotalDistance()
                                        + secondPart.getTotalDistance()
                        )
                        .estimatedTimeMinutes(
                                firstPart.getEstimatedTimeMinutes()
                                        + secondPart.getEstimatedTimeMinutes()
                                        + 5
                        )
                        .fare(
                                calculateFare(
                                        firstPart.getTotalDistance()
                                                + secondPart.getTotalDistance()
                                )
                        )
                        .interchanges(1)
                        .routeSegments(segments)
                        .build();
            }
        }

        throw new RuntimeException(
                "No route found between source and destination"
        );
    }

    private List<String> getStationsBetween(
            Long lineId,
            Integer sourceOrder,
            Integer destinationOrder
    ) {

        List<LineStation> allStations =
                lineStationRepository.findByLineIdOrderByStationOrderAsc(lineId);

        int start = Math.min(sourceOrder, destinationOrder);

        int end = Math.max(sourceOrder, destinationOrder);

        List<String> stationNames = new ArrayList<>();

        for (LineStation lineStation : allStations) {

            if (lineStation.getStationOrder() >= start
                    && lineStation.getStationOrder() <= end) {

                Station station =
                        stationRepository.findById(lineStation.getStationId())
                                .orElseThrow(() ->
                                        new RuntimeException("Station not found"));

                stationNames.add(station.getStationName());
            }
        }

        if (sourceOrder > destinationOrder) {
            Collections.reverse(stationNames);
        }

        return stationNames;
    }

    private double calculateFare(double distance) {

        if (distance <= 2) {
            return 10;
        } else if (distance <= 5) {
            return 20;
        } else if (distance <= 10) {
            return 30;
        } else if (distance <= 15) {
            return 40;
        } else {
            return 50;
        }
    }
}
