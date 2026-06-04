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
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutePlannerServiceImpl implements RoutePlannerService {

    private static final String INTERCHANGE_STATION_NAME = "Civil Court";
    private static final int MINUTES_PER_STATION = 3;
    private static final int INTERCHANGE_WAIT_MINUTES = 5;

    private static final Duration ROUTE_PLAN_CACHE_TTL = Duration.ofMinutes(30);

    /*
     * For distributed lock testing, wait time is kept higher than test delay.
     * This allows other parallel requests to wait until first request saves data in Redis.
     */
    private static final long LOCK_WAIT_TIME_SECONDS = 10;
    private static final long LOCK_LEASE_TIME_SECONDS = 20;

    /*
     * This delay is only for testing distributed lock behavior.
     * It makes first DB calculation slow so parallel requests overlap.
     *
     * After showing this to trainer/manager, change this to false.
     */
    private static final boolean ENABLE_LOCK_TEST_DELAY = false;
    private static final long LOCK_TEST_DELAY_MILLISECONDS = 5000;

    private final MetroLineRepository metroLineRepository;
    private final StationRepository stationRepository;
    private final LineStationRepository lineStationRepository;

    private final RedisTemplate<String, RoutePlanResponse> routePlanRedisTemplate;
    private final RedissonClient redissonClient;

    /**
     * Main route planning method with manual Redis cache + Redisson distributed lock.
     *
     * Production flow:
     * 1. Validate source and destination.
     * 2. Build Redis cache key.
     * 3. Check Redis cache manually.
     * 4. If cache hit, return cached response.
     * 5. If cache miss, acquire Redis distributed lock.
     * 6. After lock, double-check Redis cache again.
     * 7. If still missing, calculate route from DB.
     * 8. Save result in Redis with TTL.
     * 9. Release lock safely.
     */
    @Override
    public RoutePlanResponse planRoute(String sourceName, String destinationName) {
        validateRouteInput(sourceName, destinationName);

        String cacheKey = buildRoutePlanCacheKey(sourceName, destinationName);
        String lockKey = buildRoutePlanLockKey(sourceName, destinationName);
        String threadName = Thread.currentThread().getName();

        log.info("[ROUTE-PLAN] Request received. source={}, destination={}, thread={}",
                sourceName, destinationName, threadName);

        log.info("[CACHE-CHECK] Checking Redis cache. key={}, thread={}", cacheKey, threadName);

        RoutePlanResponse cachedResponse = getRoutePlanFromCache(cacheKey);
        if (cachedResponse != null) {
            log.info("[CACHE-HIT] Returning route from Redis. key={}, thread={}", cacheKey, threadName);
            return cachedResponse;
        }

        log.info("[CACHE-MISS] Route not found in Redis. key={}, thread={}", cacheKey, threadName);

        RLock lock = redissonClient.getLock(lockKey);
        boolean lockAcquired = false;

        try {
            log.info("[LOCK-TRY] Trying to acquire Redis lock. lockKey={}, waitTime={}s, leaseTime={}s, thread={}",
                    lockKey, LOCK_WAIT_TIME_SECONDS, LOCK_LEASE_TIME_SECONDS, threadName);

            lockAcquired = lock.tryLock(
                    LOCK_WAIT_TIME_SECONDS,
                    LOCK_LEASE_TIME_SECONDS,
                    TimeUnit.SECONDS
            );

            if (lockAcquired) {
                log.info("[LOCK-ACQUIRED] Redis lock acquired. lockKey={}, thread={}",
                        lockKey, threadName);

                /*
                 * Double-check Redis after acquiring lock.
                 * Another request may have already calculated and saved the route
                 * while this request was waiting for the lock.
                 */
                log.info("[DOUBLE-CHECK] Checking Redis again after lock. key={}, thread={}",
                        cacheKey, threadName);

                cachedResponse = getRoutePlanFromCache(cacheKey);
                if (cachedResponse != null) {
                    log.info("[DOUBLE-CHECK-HIT] Cache filled by another request. Returning Redis data. key={}, thread={}",
                            cacheKey, threadName);
                    return cachedResponse;
                }

                if (ENABLE_LOCK_TEST_DELAY) {
                    log.info("[LOCK-TEST-DELAY] Temporary delay started to prove distributed lock. delay={}ms, thread={}",
                            LOCK_TEST_DELAY_MILLISECONDS, threadName);
                    Thread.sleep(LOCK_TEST_DELAY_MILLISECONDS);
                    log.info("[LOCK-TEST-DELAY] Temporary delay completed. thread={}", threadName);
                }

                log.info("[DB-CALCULATION-START] Calculating route from DB. key={}, thread={}",
                        cacheKey, threadName);

                RoutePlanResponse freshResponse = calculateRoutePlanFromDb(sourceName, destinationName);

                log.info("[DB-CALCULATION-END] DB route calculation completed. key={}, thread={}",
                        cacheKey, threadName);

                saveRoutePlanInCache(cacheKey, freshResponse);

                log.info("[CACHE-SAVE] Route saved in Redis with TTL={} minutes. key={}, thread={}",
                        ROUTE_PLAN_CACHE_TTL.toMinutes(), cacheKey, threadName);

                return freshResponse;
            }

            /*
             * If lock is not acquired within wait time, check Redis once more.
             */
            log.warn("[LOCK-NOT-ACQUIRED] Could not acquire Redis lock within wait time. lockKey={}, thread={}",
                    lockKey, threadName);

            log.info("[CACHE-RECHECK] Rechecking Redis after lock wait failed. key={}, thread={}",
                    cacheKey, threadName);

            cachedResponse = getRoutePlanFromCache(cacheKey);
            if (cachedResponse != null) {
                log.info("[CACHE-HIT-AFTER-LOCK-WAIT] Returning route from Redis. key={}, thread={}",
                        cacheKey, threadName);
                return cachedResponse;
            }

            /*
             * Fallback:
             * API should not fail only because lock was busy.
             * This keeps availability, but in ideal test this should not happen.
             */
            log.warn("[FALLBACK-DB-CALCULATION] Lock not acquired and cache still empty. Calculating directly. key={}, thread={}",
                    cacheKey, threadName);

            return calculateRoutePlanFromDb(sourceName, destinationName);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            log.error("[LOCK-INTERRUPTED] Route planning interrupted while waiting for Redis lock. lockKey={}, thread={}",
                    lockKey, threadName, exception);

            throw new IllegalStateException(
                    "Route planning interrupted while waiting for Redis lock",
                    exception
            );

        } finally {
            if (lockAcquired && lock.isHeldByCurrentThread()) {
                log.info("[LOCK-RELEASE] Releasing Redis lock. lockKey={}, thread={}",
                        lockKey, threadName);
                lock.unlock();
                log.info("[LOCK-RELEASED] Redis lock released. lockKey={}, thread={}",
                        lockKey, threadName);
            }
        }
    }

    /**
     * Actual DB-based route calculation.
     *
     * This method contains the old route planner logic.
     * It is called only when Redis cache is missing.
     */
    private RoutePlanResponse calculateRoutePlanFromDb(String sourceName, String destinationName) {
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
     * Builds Redis cache key for route planner response.
     *
     * Example:
     * source = vanaz, destination = ramwadi
     * cache key = routePlans::vanaz::ramwadi
     */
    private String buildRoutePlanCacheKey(String sourceName, String destinationName) {
        return "routePlans::"
                + sourceName.trim().toLowerCase()
                + "::"
                + destinationName.trim().toLowerCase();
    }

    /**
     * Builds Redis lock key for route planner cache rebuild.
     *
     * Cache key stores actual route response.
     * Lock key is only used to control which request rebuilds cache.
     *
     * Example:
     * lock key = lock:routePlans::vanaz::ramwadi
     */
    private String buildRoutePlanLockKey(String sourceName, String destinationName) {
        return "lock:routePlans::"
                + sourceName.trim().toLowerCase()
                + "::"
                + destinationName.trim().toLowerCase();
    }

    /**
     * Reads route plan response from Redis manually.
     */
    private RoutePlanResponse getRoutePlanFromCache(String cacheKey) {
        return routePlanRedisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * Stores route plan response in Redis with TTL.
     *
     * TTL is 30 minutes because route plans are useful to cache,
     * but should not stay forever.
     */
    private void saveRoutePlanInCache(String cacheKey, RoutePlanResponse response) {
        routePlanRedisTemplate.opsForValue()
                .set(cacheKey, response, ROUTE_PLAN_CACHE_TTL);
    }

    /**
     * Validates user input before route planning.
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
     */
    private Station findStationByName(String stationName, String errorMessage) {
        return stationRepository.findByStationNameIgnoreCase(stationName.trim())
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage + ": " + stationName));
    }
}