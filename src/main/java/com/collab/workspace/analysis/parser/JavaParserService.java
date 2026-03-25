package com.collab.workspace.analysis.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaParserService {

	public record FileStructure(
		int totalLines,
		int blankLines,
		int commentLines,
		int classCount,
		int methodCount,
		int loopCount,
		int conditionalCount,
		int tryCatchCount,
		int ioOperationCount,
		int networkOperationCount,
		int maxNestingDepth,
		int totalMethodLength,
		int maxMethodLength
	) {
	}

	private static final Pattern METHOD_PATTERN = Pattern.compile(
		"(public|protected|private|static|final|synchronized|native|abstract|\\s)+[\\w<>\\[\\]]+\\s+(\\w+)\\s*\\([^;]*\\)\\s*\\{"
	);

	public FileStructure parse(String content) {
		String[] lines = content.split("\\R", -1);
		int blankLines = 0;
		int commentLines = 0;
		for (String raw : lines) {
			String line = raw.trim();
			if (line.isBlank()) {
				blankLines++;
			}
			if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) {
				commentLines++;
			}
		}

		int classCount = countMatches(content, "\\b(class|interface|record|enum)\\b");
		int methodCount = 0;
		int totalMethodLength = 0;
		int maxMethodLength = 0;

		Matcher matcher = METHOD_PATTERN.matcher(content);
		while (matcher.find()) {
			methodCount++;
			int startBraceIndex = content.indexOf('{', matcher.end() - 1);
			int methodLength = estimateBlockLength(content, startBraceIndex);
			totalMethodLength += methodLength;
			maxMethodLength = Math.max(maxMethodLength, methodLength);
		}

		return new FileStructure(
			lines.length,
			blankLines,
			commentLines,
			classCount,
			methodCount,
			countMatches(content, "\\b(for|while|do)\\b"),
			countMatches(content, "\\b(if|switch|case)\\b"),
			countMatches(content, "\\b(try|catch)\\b"),
			countMatches(content, "\\b(Files\\.|FileInputStream|FileOutputStream|BufferedReader|BufferedWriter|read\\(|write\\()"),
			countMatches(content, "\\b(Socket|ServerSocket|HttpClient|URLConnection|DatagramSocket|connect\\()"),
			computeMaxNestingDepth(content),
			totalMethodLength,
			maxMethodLength
		);
	}

	private int countMatches(String content, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(content);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	private int computeMaxNestingDepth(String content) {
		int depth = 0;
		int max = 0;
		for (char value : content.toCharArray()) {
			if (value == '{') {
				depth++;
				max = Math.max(max, depth);
			} else if (value == '}') {
				depth = Math.max(0, depth - 1);
			}
		}
		return max;
	}

	private int estimateBlockLength(String content, int openingBraceIndex) {
		if (openingBraceIndex < 0 || openingBraceIndex >= content.length()) {
			return 0;
		}
		int depth = 0;
		int endIndex = content.length() - 1;
		for (int i = openingBraceIndex; i < content.length(); i++) {
			char value = content.charAt(i);
			if (value == '{') {
				depth++;
			} else if (value == '}') {
				depth--;
				if (depth == 0) {
					endIndex = i;
					break;
				}
			}
		}
		return lineNumberAt(content, endIndex) - lineNumberAt(content, openingBraceIndex) + 1;
	}

	private int lineNumberAt(String content, int index) {
		int line = 1;
		for (int i = 0; i < Math.min(index, content.length()); i++) {
			if (content.charAt(i) == '\n') {
				line++;
			}
		}
		return line;
	}
}
