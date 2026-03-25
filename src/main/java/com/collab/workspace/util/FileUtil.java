package com.collab.workspace.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

public class FileUtil {

	private FileUtil() {
	}

	public static void validateJavaUpload(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A .java file is required");
		}

		String originalName = file.getOriginalFilename();
		if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".java")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only .java files are allowed");
		}
	}

	public static String detectLanguage(String filePath) {
		if (filePath == null) {
			return "text";
		}
		String lower = filePath.toLowerCase(Locale.ROOT);
		if (lower.endsWith(".java")) {
			return "java";
		}
		if (lower.endsWith(".js") || lower.endsWith(".jsx") || lower.endsWith(".ts") || lower.endsWith(".tsx")) {
			return "javascript";
		}
		if (lower.endsWith(".json")) {
			return "json";
		}
		if (lower.endsWith(".xml")) {
			return "xml";
		}
		return "text";
	}

	public static String sanitizeDownloadFileName(String filePath) {
		if (filePath == null || filePath.isBlank()) {
			return "code.java";
		}
		String normalized = filePath.replace('\\', '/');
		String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
		fileName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
		if (fileName.isBlank()) {
			return "code.java";
		}
		return fileName;
	}
}
