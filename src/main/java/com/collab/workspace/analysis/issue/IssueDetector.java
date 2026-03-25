package com.collab.workspace.analysis.issue;

import com.collab.workspace.analysis.model.CodeIssue;
import com.collab.workspace.analysis.model.IssueType;
import com.collab.workspace.analysis.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class IssueDetector {

	public List<CodeIssue> detect(String filePath, String content) {
		List<CodeIssue> issues = new ArrayList<>();
		String[] lines = content.split("\\R");
		int nesting = 0;

		for (int index = 0; index < lines.length; index++) {
			String trimmed = lines[index].trim();

			if (trimmed.contains("{")) {
				nesting += countMatches(trimmed, "\\{");
			}
			if (trimmed.contains("}")) {
				nesting -= countMatches(trimmed, "}");
				nesting = Math.max(0, nesting);
			}

			if (trimmed.matches(".*for\\s*\\(.*\\).*") && index + 1 < lines.length && lines[index + 1].contains("+=")) {
				issues.add(issue(
					"perf-string-concat-" + filePath + "-" + index,
					IssueType.PERFORMANCE,
					Severity.MEDIUM,
					filePath,
					index + 1,
					"Potential expensive concatenation in loop",
					"String concatenation inside loops creates many intermediate objects.",
					"Use StringBuilder and append values inside the loop.",
					"StringBuilder builder = new StringBuilder();",
					"Avoidable memory churn and slower runtime."
				));
			}

			if (trimmed.matches("catch\\s*\\(\\s*Exception\\s+\\w+\\s*\\).*")) {
				issues.add(issue(
					"broad-catch-" + filePath + "-" + index,
					IssueType.MAINTAINABILITY,
					Severity.MEDIUM,
					filePath,
					index + 1,
					"Broad exception catch",
					"Catching generic Exception hides the actual failure modes.",
					"Catch specific checked or runtime exceptions instead.",
					null,
					"Harder debugging and weaker error handling."
				));
			}

			if (trimmed.matches("catch\\s*\\(.*\\)\\s*\\{\\s*}")) {
				issues.add(issue(
					"empty-catch-" + filePath + "-" + index,
					IssueType.SECURITY,
					Severity.HIGH,
					filePath,
					index + 1,
					"Empty catch block",
					"Ignoring exceptions can hide corrupted state or failed I/O.",
					"Log the exception or translate it into a domain-specific failure.",
					null,
					"Silent failures and inconsistent behavior."
				));
			}

			if (trimmed.contains("System.out.println")) {
				issues.add(issue(
					"stdout-" + filePath + "-" + index,
					IssueType.STYLE,
					Severity.LOW,
					filePath,
					index + 1,
					"Console logging in application code",
					"Direct console output is difficult to filter and test in production.",
					"Use a structured logger such as SLF4J.",
					null,
					"Noisy logs and limited observability."
				));
			}

			if (nesting >= 5 && trimmed.contains("if")) {
				issues.add(issue(
					"nesting-" + filePath + "-" + index,
					IssueType.MAINTAINABILITY,
					Severity.HIGH,
					filePath,
					index + 1,
					"Deep nesting detected",
					"The control flow is becoming hard to follow.",
					"Extract nested branches into smaller methods or return early.",
					null,
					"Higher bug risk and lower readability."
				));
			}
		}

		return issues;
	}

	private int countMatches(String content, String regex) {
		var matcher = Pattern.compile(regex).matcher(content);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	private CodeIssue issue(
		String id,
		IssueType type,
		Severity severity,
		String filePath,
		long line,
		String title,
		String explanation,
		String suggestedFix,
		String fixedSnippet,
		String impact
	) {
		CodeIssue issue = new CodeIssue();
		issue.setId(id);
		issue.setType(type);
		issue.setSeverity(severity);
		issue.setFilePath(filePath);
		issue.setLine(line);
		issue.setTitle(title);
		issue.setExplanation(explanation);
		issue.setSuggestedFix(suggestedFix);
		issue.setFixedSnippet(fixedSnippet);
		issue.setImpact(impact);
		return issue;
	}
}
