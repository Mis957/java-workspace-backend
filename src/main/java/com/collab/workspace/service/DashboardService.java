package com.collab.workspace.service;

import com.collab.workspace.entity.AnalysisReport;
import com.collab.workspace.entity.Room;
import com.collab.workspace.entity.User;
import com.collab.workspace.entity.Version;
import com.collab.workspace.entity.WorkspaceFile;
import com.collab.workspace.repository.AnalysisReportRepository;
import com.collab.workspace.repository.RoomMemberRepository;
import com.collab.workspace.repository.RoomRepository;
import com.collab.workspace.repository.UserRepository;
import com.collab.workspace.repository.VersionRepository;
import com.collab.workspace.repository.WorkspaceFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final RoomMemberRepository roomMemberRepository;
	private final WorkspaceFileRepository workspaceFileRepository;
	private final VersionRepository versionRepository;
	private final AnalysisReportRepository analysisReportRepository;

	public DashboardService(
		UserRepository userRepository,
		RoomRepository roomRepository,
		RoomMemberRepository roomMemberRepository,
		WorkspaceFileRepository workspaceFileRepository,
		VersionRepository versionRepository,
		AnalysisReportRepository analysisReportRepository
	) {
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
		this.roomMemberRepository = roomMemberRepository;
		this.workspaceFileRepository = workspaceFileRepository;
		this.versionRepository = versionRepository;
		this.analysisReportRepository = analysisReportRepository;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getDashboard(String currentUserEmail) {
		User currentUser = getUserByEmail(currentUserEmail);
		List<Room> rooms = roomRepository.findAllByParticipantUserId(currentUser.getId());
		List<Long> roomIds = rooms.stream().map(Room::getId).toList();

		long totalRooms = rooms.size();
		long totalFiles = roomIds.isEmpty() ? 0L : workspaceFileRepository.countByRoom_IdIn(roomIds);
		long totalVersions = roomIds.isEmpty() ? 0L : versionRepository.countByFile_Room_IdIn(roomIds);
		long roomScopedAnalyses = roomIds.isEmpty() ? 0L : analysisReportRepository.countByFile_Room_IdIn(roomIds);
		long totalAnalyses = roomScopedAnalyses > 0 ? roomScopedAnalyses : analysisReportRepository.count();

		Double avgScoreDb = roomIds.isEmpty() ? null : analysisReportRepository.averagePerformanceByRoomIds(roomIds);
		Integer bestScoreDb = roomIds.isEmpty() ? null : analysisReportRepository.maxPerformanceByRoomIds(roomIds);
		AnalysisReport latestRoomReport = roomIds.isEmpty() ? null : analysisReportRepository.findTopByFile_Room_IdInOrderByCreatedAtDesc(roomIds);
		AnalysisReport latestGlobalReport = analysisReportRepository.findTopByOrderByCreatedAtDesc().orElse(null);

		double averagePerformance = avgScoreDb == null ? (latestGlobalReport == null ? 0.0 : latestGlobalReport.getPerformanceScore()) : avgScoreDb;
		int bestPerformance = bestScoreDb == null ? (latestGlobalReport == null ? 0 : latestGlobalReport.getPerformanceScore()) : bestScoreDb;
		AnalysisReport riskSource = latestRoomReport != null ? latestRoomReport : latestGlobalReport;
		String latestRiskLevel = riskSource == null || riskSource.getRiskLevel() == null
			? "UNKNOWN"
			: riskSource.getRiskLevel();

		List<Map<String, Object>> roomSummaries = rooms.stream().map(this::toRoomSummary).toList();
		List<Map<String, Object>> recentActivity = buildRecentActivity(rooms, roomIds);

		Map<String, Object> totals = new LinkedHashMap<>();
		totals.put("rooms", totalRooms);
		totals.put("files", totalFiles);
		totals.put("versions", totalVersions);
		totals.put("analyses", totalAnalyses);

		Map<String, Object> performance = new LinkedHashMap<>();
		performance.put("averageScore", Math.round(averagePerformance * 100.0) / 100.0);
		performance.put("bestScore", bestPerformance);
		performance.put("latestRiskLevel", latestRiskLevel);

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("totals", totals);
		response.put("performance", performance);
		response.put("rooms", roomSummaries);
		response.put("recentActivity", recentActivity);
		return response;
	}

	private List<Map<String, Object>> buildRecentActivity(List<Room> rooms, List<Long> roomIds) {
		if (roomIds.isEmpty()) {
			return List.of();
		}

		List<Map<String, Object>> activity = new ArrayList<>();

		List<Version> versions = versionRepository.findTop20ByFile_Room_IdInOrderByCreatedAtDesc(roomIds);
		for (Version version : versions) {
			if (version.getFile() == null || version.getFile().getRoom() == null) {
				continue;
			}
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("type", "VERSION_SAVED");
			entry.put("title", "Version v" + version.getVersionNumber() + " saved");
			entry.put("description", version.getFile().getFilePath() + " in " + version.getFile().getRoom().getRoomName());
			entry.put("createdAt", version.getCreatedAt());
			activity.add(entry);
		}

		List<AnalysisReport> reports = analysisReportRepository.findTop20ByFile_Room_IdInOrderByCreatedAtDesc(roomIds);
		if (reports.isEmpty()) {
			reports = analysisReportRepository.findTop20ByOrderByCreatedAtDesc();
		}
		for (AnalysisReport report : reports) {
			WorkspaceFile file = report.getFile();
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("type", "ANALYSIS_RUN");
			entry.put("title", "Analysis completed");
			String description = file != null && file.getRoom() != null
				? file.getFilePath() + " score " + report.getPerformanceScore()
				: "Workspace score " + report.getPerformanceScore();
			entry.put("description", description);
			entry.put("createdAt", report.getCreatedAt());
			activity.add(entry);
		}

		for (Room room : rooms.stream().limit(10).toList()) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("type", "ROOM_JOINED");
			entry.put("title", "Room available");
			entry.put("description", room.getRoomName() + " (" + room.getRoomCode() + ")");
			entry.put("createdAt", room.getCreatedAt());
			activity.add(entry);
		}

		return activity.stream()
			.sorted(Comparator.comparing(this::activityTime).reversed())
			.limit(20)
			.toList();
	}

	private LocalDateTime activityTime(Map<String, Object> entry) {
		Object value = entry.get("createdAt");
		if (value instanceof LocalDateTime dateTime) {
			return dateTime;
		}
		return LocalDateTime.MIN;
	}

	private Map<String, Object> toRoomSummary(Room room) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("id", room.getId());
		response.put("roomCode", room.getRoomCode());
		response.put("roomName", room.getRoomName());
		response.put("createdAt", room.getCreatedAt());
		response.put("ownerEmail", room.getOwner() != null ? room.getOwner().getEmail() : null);
		response.put("memberCount", roomMemberRepository.countByRoom_Id(room.getId()));
		response.put("fileCount", workspaceFileRepository.countByRoom_Id(room.getId()));
		return response;
	}

	private User getUserByEmail(String email) {
		String normalized = required(email, "Authenticated user email is missing")
			.trim()
			.toLowerCase(Locale.ROOT);

		return userRepository.findByEmailIgnoreCase(normalized)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
	}

	private String required(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
		}
		return value;
	}
}
