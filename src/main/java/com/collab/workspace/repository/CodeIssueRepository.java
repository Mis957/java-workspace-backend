package com.collab.workspace.repository;

import com.collab.workspace.entity.CodeIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeIssueRepository extends JpaRepository<CodeIssue, Long> {

	List<CodeIssue> findAllByReport_IdOrderByCreatedAtDesc(Long reportId);

	void deleteByReport_Id(Long reportId);
}
