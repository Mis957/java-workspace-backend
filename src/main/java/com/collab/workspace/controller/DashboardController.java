package com.collab.workspace.controller;

import com.collab.workspace.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/dashboard")
public class DashboardController {
	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> getDashboard(HttpServletRequest request) {
		return ResponseEntity.ok(dashboardService.getDashboard(getEmail(request)));
	}

	private String getEmail(HttpServletRequest request) {
		Object email = request.getAttribute("authUserEmail");
		if (email == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated request");
		}
		return email.toString();
	}
}
