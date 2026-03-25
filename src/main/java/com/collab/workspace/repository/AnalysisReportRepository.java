package com.collab.workspace.repository;

import com.collab.workspace.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {

	List<AnalysisReport> findAllByFile_IdOrderByCreatedAtDesc(Long fileId);
}
