package com.metrosewa.route_service.service;

import com.metrosewa.route_service.entity.MetroLine;

import java.util.List;

public interface MetroLineService {

    List<MetroLine> getAllLines();

    MetroLine createLine(MetroLine metroLine);

    MetroLine updateLine(Long id, MetroLine updatedLine);

    void deleteLine(Long id);
}
