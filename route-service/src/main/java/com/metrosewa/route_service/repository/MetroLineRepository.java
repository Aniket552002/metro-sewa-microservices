package com.metrosewa.route_service.repository;

import com.metrosewa.route_service.entity.MetroLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetroLineRepository extends JpaRepository<MetroLine, Long> {
    List<MetroLine> findByActiveTrue();
}