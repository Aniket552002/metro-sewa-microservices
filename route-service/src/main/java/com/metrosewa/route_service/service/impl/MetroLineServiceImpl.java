package com.metrosewa.route_service.service.impl;

import com.metrosewa.route_service.entity.MetroLine;
import com.metrosewa.route_service.repository.MetroLineRepository;
import com.metrosewa.route_service.service.MetroLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * This service handles metro line master data.
 *
 * It is used to create, update, delete, and fetch metro lines.
 */
@Service
@RequiredArgsConstructor
public class MetroLineServiceImpl implements MetroLineService {

    private final MetroLineRepository metroLineRepository;

    /**
     * Redis cache is used here because metro lines are requested repeatedly.
     *
     * First request:
     * - Data comes from MariaDB
     * - Result is stored in Redis cache
     *
     * Second request:
     * - Data comes directly from Redis
     * - Database query is skipped
     */
    @Override
    @Cacheable(value = "metroLines")
    public List<MetroLine> getAllLines() {

        System.out.println("Fetching metro lines from database...");
        log.info("Fetching metro lines from MariaDB");
        return metroLineRepository.findByActiveTrue();
    }

    /**
     * When a new line is created, metroLines cache is cleared.
     */
    @Override
    @CacheEvict(value = "metroLines", allEntries = true)
    public MetroLine createLine(MetroLine metroLine) {
        return metroLineRepository.save(metroLine);
    }

    /**
     * When line data is updated, metroLines cache is cleared.
     */
    @Override
    @CacheEvict(value = "metroLines", allEntries = true)
    public MetroLine updateLine(Long id, MetroLine updatedLine) {

        MetroLine existingLine = metroLineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Metro line not found"
                ));

        existingLine.setLineName(updatedLine.getLineName());
        existingLine.setLineNumber(updatedLine.getLineNumber());
        existingLine.setColorCode(updatedLine.getColorCode());
        existingLine.setActive(updatedLine.getActive());

        return metroLineRepository.save(existingLine);
    }

    /**
     * When line is deleted, metroLines cache is cleared.
     */
    @Override
    @CacheEvict(value = "metroLines", allEntries = true)
    public void deleteLine(Long id) {
        metroLineRepository.deleteById(id);
    }
}