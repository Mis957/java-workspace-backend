package com.collab.workspace.controller;

import com.collab.workspace.analysis.OptimizationResult;
import com.collab.workspace.analysis.model.AnalysisResult;
import com.collab.workspace.analysis.model.FullReviewResponse;
import com.collab.workspace.dto.WorkspaceRequest;
import com.collab.workspace.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
public class AnalysisController {

	private final AnalysisService analysisService;

	public AnalysisController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	@PostMapping("/optimizer/java")
	public ResponseEntity<OptimizationResult> optimize(@RequestBody WorkspaceRequest request) {
		return ResponseEntity.ok(analysisService.optimize(request));
	}

	@PostMapping("/analyzer/java")
	public ResponseEntity<AnalysisResult> analyze(@RequestBody WorkspaceRequest request) {
		return ResponseEntity.ok(analysisService.analyze(request));
	}

	@PostMapping("/analyzer/java/full")
	public ResponseEntity<FullReviewResponse> fullReview(@RequestBody WorkspaceRequest request) {
		return ResponseEntity.ok(analysisService.fullReview(request));
	}

	@GetMapping("/meta/rules")
	public ResponseEntity<Map<String, Object>> rules() {
		return ResponseEntity.ok(Map.of(
			"supportedLanguage", "java",
			"optimizerRules", List.of(
				"deep nesting detection",
				"broad catch detection",
				"empty catch detection",
				"console logging hints",
				"loop concatenation warnings"
			),
			"analysisMetrics", List.of(
				"cyclomatic complexity",
				"nesting depth",
				"method length",
				"estimated time complexity",
				"maintainability index",
				"performance score",
				"I/O operation count",
				"network operation count"
			)
		));
	}

	@GetMapping("/meta/health")
	public ResponseEntity<Map<String, Object>> health() {
		return ResponseEntity.ok(Map.of("status", "UP", "service", "analysis"));
	}
}
