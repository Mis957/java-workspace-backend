package com.collab.workspace.service;

import com.collab.workspace.analysis.AnalysisEngine;
import com.collab.workspace.analysis.OptimizationResult;
import com.collab.workspace.analysis.model.AnalysisResult;
import com.collab.workspace.analysis.model.CodeIssue;
import com.collab.workspace.analysis.model.FullReviewResponse;
import com.collab.workspace.dto.WorkspaceRequest;
import com.collab.workspace.entity.AnalysisReport;
import com.collab.workspace.repository.AnalysisReportRepository;
import com.collab.workspace.repository.CodeIssueRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AnalysisService {

	private final AnalysisEngine analysisEngine = new AnalysisEngine();
	private final AnalysisReportRepository analysisReportRepository;
	private final CodeIssueRepository codeIssueRepository;

	public AnalysisService(AnalysisReportRepository analysisReportRepository, CodeIssueRepository codeIssueRepository) {
		this.analysisReportRepository = analysisReportRepository;
		this.codeIssueRepository = codeIssueRepository;
	}

	public OptimizationResult optimize(WorkspaceRequest request) {
		validate(request);
		return analysisEngine.optimize(request);
	}

	public AnalysisResult analyze(WorkspaceRequest request) {
		validate(request);
		OptimizationResult optimization = analysisEngine.optimize(request);
		AnalysisResult analysis = analysisEngine.analyze(request, optimization);
		storeReport(analysis, optimization);
		return analysis;
	}

	public FullReviewResponse fullReview(WorkspaceRequest request) {
		validate(request);
		FullReviewResponse response = analysisEngine.fullReview(request);
		storeReport(response.getAnalysis(), response.getOptimization());
		return response;
	}

	private void validate(WorkspaceRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request body is required.");
		}
		if (request.getWorkspaceName() == null || request.getWorkspaceName().isBlank()) {
			throw new IllegalArgumentException("workspaceName is required.");
		}
		if (request.getEntryFile() == null || request.getEntryFile().isBlank()) {
			throw new IllegalArgumentException("entryFile is required.");
		}
		if (request.getFiles() == null || request.getFiles().isEmpty()) {
			throw new IllegalArgumentException("At least one Java source file is required.");
		}
	}

	private void storeReport(AnalysisResult analysis, OptimizationResult optimization) {
		AnalysisReport report = new AnalysisReport();
		report.setCyclomaticComplexity(analysis.getComplexity().getCyclomaticComplexity());
		report.setTimeComplexity(analysis.getComplexity().getEstimatedTimeComplexity());
		report.setPerformanceScore(analysis.getComplexity().getPerformanceScore());
		report.setRiskLevel(analysis.getComplexity().getRiskLevel());
		report = analysisReportRepository.save(report);

		for (CodeIssue issue : optimization.getIssues()) {
			com.collab.workspace.entity.CodeIssue dbIssue = new com.collab.workspace.entity.CodeIssue();
			dbIssue.setIssueType(issue.getType() == null ? null : issue.getType().name());
			dbIssue.setSeverity(issue.getSeverity() == null ? null : issue.getSeverity().name().toLowerCase(Locale.ROOT));
			dbIssue.setMessage(issue.getTitle() + ": " + issue.getExplanation());
			dbIssue.setLineNumber(issue.getLine());
			dbIssue.setRecommendation(issue.getSuggestedFix());
			dbIssue.setReport(report);
			codeIssueRepository.save(dbIssue);
		}
	}
}
