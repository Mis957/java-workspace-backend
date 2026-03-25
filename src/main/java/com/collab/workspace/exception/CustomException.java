package com.collab.workspace.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	public CustomException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
		this.code = code == null || code.isBlank() ? "CUSTOM_ERROR" : code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}
}
