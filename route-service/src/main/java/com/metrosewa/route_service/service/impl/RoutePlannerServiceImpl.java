package com.metrosewa.route_service.service.impl;

import com.metrosewa.route_service.dto.RoutePlanResponse;
import com.metrosewa.route_service.dto.RouteSegment;
import com.metrosewa.route_service.entity.LineStation;
import com.metrosewa.route_service.entity.MetroLine;
import com.metrosewa.route_service.entity.Station;
import com.metrosewa.route_service.exception.BadRequestException;
import com.metrosewa.route_service.exception.ResourceNotFoundException;
import com.metrosewa.route_service.repository.LineStationRepository;
import com.metrosewa.route_service.repository.MetroLineRepository;
import com.metrosewa.route_service.repository.StationRepository;
import com.metrosewa.route_service.service.RoutePlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutePlannerServiceImpl implements RoutePlannerService {

    private static final String INTERCHANGE_STATION_NAME = "Civil Court";
    private static final int MINUTES_PER_STATION = 3;
    private static final int INTERCHANGE_WAIT_MINUTES = 5;

    private final MetroLineRepository metroLineRepository;
    private final StationRepository stationRepository;
    private final LineStationRepository lineStationRepository;

    /**
     * Main route planning method.
     *
     * Flow:
     * 1. Validate source and destination input.
     * 2. Find source and destination stations from database.
     * 3. Get all metro lines connected to source and destination.
     * 4. Check if both stations are on the same line using HashMap lookup.
     * 5. If same line exists, return direct route.
     * 6. If same line does not exist, plan route using Civil Court interchange.
     */
    @Override
    public RoutePlanResponse planRoute(String sourceName, String destinationName) {
        validateRouteInput(sourceName, destinationName);

        Station source = findStationByName(sourceName, "Source station not found");
        Station destination = findStationByName(destinationName, "Destination station not found");

        List<LineStation> sourceLines =
                lineStationRepository.findByStationId(source.getId());

        List<LineStation> destinationLines =
                lineStationRepository.findByStationId(destination.getId());

        /*
         * Direct route optimization:
         * Old logic used nested loops: O(S x D).
         * New logic stores source lines in HashMap by lineId.
         * Destination line is checked directly in map.
         * Time complexity becomes O(S + D), with average O(1) lookup.
         */
        Map<Long, LineStation> sourceLineMap = sourceLines.stream()
                .collect(Collectors.toMap(
                        LineStation::getLineId,
                        lineStation -> lineStation
                ));

        for (LineStation destinationLine : destinationLines) {
            LineStation sourceLine = sourceLineMap.get(destinationLine.getLineId());

            if (sourceLine != null) {
                return buildDirectRoute(
                        source,
                        destination,
                        sourceLine,
                        destinationLine
                );
            }
        }

        /*
         * If no common lineId is found, source and destination are on different lines.
         * In that case, route is planned using Civil Court as interchange station.
         */
        return buildInterchangeRoute(
                source,
                destination,
                sourceLines,
                destinationLines
        );
    }

    /**
     * Validates user input before route planning.
     *
     * Empty source, empty destination, or same source/destination is a client-side mistake,
     * so BadRequestException is thrown and API returns 400 Bad Request.
     */
    private void validateRouteInput(String sourceName, String destinationName) {
        if (sourceName == null || sourceName.isBlank()) {
            throw new BadRequestException("Source station is required");
        }

        if (destinationName == null || destinationName.isBlank()) {
            throw new BadRequestException("Destination station is required");
        }

        if (sourceName.trim().equalsIgnoreCase(destinationName.trim())) {
            throw new BadRequestException("Source and destination cannot be same");
        }
    }

    /**
     * Builds route response when source and destination are on the same metro line.
     *
     * This method calculates:
     * 1. Stations between source and destination.
     * 2. Total station count.
     * 3. Total distance.
     * 4. Estimated travel time.
     * 5. Fare.
     * 6. Single route segment because no interchange is required.
     */
    private RoutePlanResponse buildDirectRoute(
            Station source,
            Station destination,
            LineStation sourceLineStation,
            LineStation destinationLineStation
    ) {
        MetroLine line = metroLineRepository.findById(sourceLineStation.getLineId())
                .orElseThrow(() -> new ResourceNotFoundException("Line not found"));

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

        int estimatedTime = totalStations * MINUTES_PER_STATION;
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

    /**
     * Builds route response when source and destination are on different metro lines.
     *
     * Current business logic:
     * 1. Civil Court is used as the fixed interchange station.
     * 2. Get all line mappings of Civil Court.
     * 3. Store Civil Court line mappings in HashMap by lineId.
     * 4. Check if source line connects to Civil Court.
     * 5. Check if destination line connects to Civil Court.
     * 6. If both are connected, return two route parts:
     *    - Source -> Civil Court
     *    - Civil Court -> Destination
     *
     * This avoids nested source/destination line comparison and avoids repeated DB calls inside loop.
     */
    private RoutePlanResponse buildInterchangeRoute(
            Station source,
            Station destination,
            List<LineStation> sourceLines,
            List<LineStation> destinationLines
    ) {
        Station interchange = findStationByName(
                INTERCHANGE_STATION_NAME,
                "Interchange station not found"
        );

        List<LineStation> interchangeLines =
                lineStationRepository.findByStationId(interchange.getId());

        Map<Long, LineStation> interchangeLineMap = interchangeLines.stream()
                .collect(Collectors.toMap(
                        LineStation::getLineId,
                        lineStation -> lineStation
                ));

        LineStation selectedSourceLine = null;
        LineStation sourceToInterchange = null;

        for (LineStation sourceLine : sourceLines) {
            LineStation interchangeOnSourceLine =
                    interchangeLineMap.get(sourceLine.getLineId());

            if (interchangeOnSourceLine != null) {
                selectedSourceLine = sourceLine;
                sourceToInterchange = interchangeOnSourceLine;
                break;
            }
        }

        LineStation selectedDestinationLine = null;
        LineStation interchangeToDestination = null;

        for (LineStation destinationLine : destinationLines) {
            LineStation interchangeOnDestinationLine =
                    interchangeLineMap.get(destinationLine.getLineId());

            if (interchangeOnDestinationLine != null) {
                selectedDestinationLine = destinationLine;
                interchangeToDestination = interchangeOnDestinationLine;
                break;
            }
        }

        if (selectedSourceLine == null || selectedDestinationLine == null) {
            throw new ResourceNotFoundException("No route found between source and destination");
        }

        RoutePlanResponse firstPart =
                buildDirectRoute(
                        source,
                        interchange,
                        selectedSourceLine,
                        sourceToInterchange
                );

        RoutePlanResponse secondPart =
                buildDirectRoute(
                        interchange,
                        destination,
                        interchangeToDestination,
                        selectedDestinationLine
                );

        List<RouteSegment> segments = new ArrayList<>();
        segments.addAll(firstPart.getRouteSegments());
        segments.addAll(secondPart.getRouteSegments());

        double totalDistance =
                firstPart.getTotalDistance() + secondPart.getTotalDistance();

        return RoutePlanResponse.builder()
                .source(source.getStationName())
                .destination(destination.getStationName())
                .totalStations(
                        firstPart.getTotalStations()
                                + secondPart.getTotalStations()
                )
                .totalDistance(totalDistance)
                .estimatedTimeMinutes(
                        firstPart.getEstimatedTimeMinutes()
                                + secondPart.getEstimatedTimeMinutes()
                                + INTERCHANGE_WAIT_MINUTES
                )
                .fare(calculateFare(totalDistance))
                .interchanges(1)
                .routeSegments(segments)
                .build();
    }

    /**
     * Returns station names between source station order and destination station order.
     *
     * Old logic:
     * - Fetched all stations of the line.
     * - Filtered stations in Java.
     * - Called stationRepository.findById() inside loop.
     *
     * New logic:
     * - Repository query joins LineStation and Station.
     * - Database filters by lineId and stationOrder range.
     * - Only required station names are returned.
     */
    private List<String> getStationsBetween(
            Long lineId,
            Integer sourceOrder,
            Integer destinationOrder
    ) {
        int start = Math.min(sourceOrder, destinationOrder);
        int end = Math.max(sourceOrder, destinationOrder);

        List<String> stationNames =
                lineStationRepository.findStationNamesBetweenOrders(
                        lineId,
                        start,
                        end
                );

        if (sourceOrder > destinationOrder) {
            Collections.reverse(stationNames);
        }

        return stationNames;
    }

    /**
     * Calculates fare using simple distance slabs.
     *
     * Current fare rules:
     * 0 - 2 km      = 10
     * 2 - 5 km      = 20
     * 5 - 10 km     = 30
     * 10 - 15 km    = 40
     * Above 15 km   = 50
     */
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

    /**
     * Finds station by station name.
     *
     * If station is not found, ResourceNotFoundException is thrown.
     * GlobalExceptionHandler converts that exception into 404 Not Found response.
     */
    private Station findStationByName(String stationName, String errorMessage) {
        return stationRepository.findByStationNameIgnoreCase(stationName.trim())
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage + ": " + stationName));
    }
}