package com.collab.workspace.repository;

import com.collab.workspace.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {

	List<AnalysisReport> findAllByFile_IdOrderByCreatedAtDesc(Long fileId);

	long countByFile_Room_IdIn(List<Long> roomIds);

	List<AnalysisReport> findTop20ByFile_Room_IdInOrderByCreatedAtDesc(List<Long> roomIds);

	AnalysisReport findTopByFile_Room_IdInOrderByCreatedAtDesc(List<Long> roomIds);

	List<AnalysisReport> findTop20ByOrderByCreatedAtDesc();

	Optional<AnalysisReport> findTopByOrderByCreatedAtDesc();

	@Query("select avg(ar.performanceScore) from AnalysisReport ar where ar.file.room.id in :roomIds")
	Double averagePerformanceByRoomIds(List<Long> roomIds);

	@Query("select max(ar.performanceScore) from AnalysisReport ar where ar.file.room.id in :roomIds")
	Integer maxPerformanceByRoomIds(List<Long> roomIds);
}
