package com.collab.workspace.analysis.complexity;

import com.collab.workspace.analysis.model.ComplexitySummary;

public class ComplexityCalculator {

	public void finalizeMetrics(ComplexitySummary summary, int issueCount) {
		summary.setEstimatedTimeComplexity(estimateTimeComplexity(summary.getLoopCount(), summary.getMaxNestingDepth()));
		summary.setRiskLevel(determineRiskLevel(summary.getCyclomaticComplexity(), issueCount, summary.getMaxNestingDepth()));
		summary.setMaintainabilityIndex(estimateMaintainability(summary, issueCount));
		summary.setPerformanceScore(estimatePerformanceScore(summary, issueCount));
	}

	public void accumulate(ComplexitySummary target, ComplexitySummary source) {
		target.setTotalFiles(target.getTotalFiles() + source.getTotalFiles());
		target.setTotalLines(target.getTotalLines() + source.getTotalLines());
		target.setCodeLines(target.getCodeLines() + source.getCodeLines());
		target.setBlankLines(target.getBlankLines() + source.getBlankLines());
		target.setCommentLines(target.getCommentLines() + source.getCommentLines());
		target.setClassCount(target.getClassCount() + source.getClassCount());
		target.setMethodCount(target.getMethodCount() + source.getMethodCount());
		target.setLoopCount(target.getLoopCount() + source.getLoopCount());
		target.setConditionalCount(target.getConditionalCount() + source.getConditionalCount());
		target.setTryCatchCount(target.getTryCatchCount() + source.getTryCatchCount());
		target.setIoOperationCount(target.getIoOperationCount() + source.getIoOperationCount());
		target.setNetworkOperationCount(target.getNetworkOperationCount() + source.getNetworkOperationCount());
		target.setCyclomaticComplexity(target.getCyclomaticComplexity() + source.getCyclomaticComplexity());
		target.setMaxNestingDepth(Math.max(target.getMaxNestingDepth(), source.getMaxNestingDepth()));
		target.setMaxMethodLength(Math.max(target.getMaxMethodLength(), source.getMaxMethodLength()));
	}

	private String estimateTimeComplexity(int loopCount, int maxNestingDepth) {
		if (loopCount >= 2 && maxNestingDepth >= 3) {
			return "O(n^2)";
		}
		if (loopCount > 0 && maxNestingDepth > 2) {
			return "O(n log n) to O(n^2)";
		}
		if (loopCount > 0) {
			return "O(n)";
		}
		return "O(1)";
	}

	private String determineRiskLevel(int cyclomatic, int issueCount, int maxNestingDepth) {
		if (cyclomatic > 25 || issueCount > 8 || maxNestingDepth >= 5) {
			return "High";
		}
		if (cyclomatic > 12 || issueCount > 3 || maxNestingDepth >= 3) {
			return "Medium";
		}
		return "Low";
	}

	private double estimateMaintainability(ComplexitySummary summary, int issueCount) {
		double raw = 100
			- (summary.getCyclomaticComplexity() * 1.4)
			- (summary.getAverageMethodLength() * 0.8)
			- (summary.getMaxNestingDepth() * 4.0)
			- (issueCount * 2.5);
		return round(Math.max(5, Math.min(100, raw)));
	}

	private int estimatePerformanceScore(ComplexitySummary summary, int issueCount) {
		double score = 100
			- (summary.getLoopCount() * 3.5)
			- (summary.getIoOperationCount() * 2.0)
			- (summary.getNetworkOperationCount() * 2.5)
			- (issueCount * 1.8);
		return Math.max(1, Math.min(100, (int) Math.round(score)));
	}

	private double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
