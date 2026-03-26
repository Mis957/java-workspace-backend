package com.collab.workspace.repository;

import com.collab.workspace.entity.WorkspaceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceFileRepository extends JpaRepository<WorkspaceFile, Long> {

	List<WorkspaceFile> findAllByRoom_IdOrderByUpdatedAtDesc(Long roomId);

	List<WorkspaceFile> findAllByRoom_Id(Long roomId);

	Optional<WorkspaceFile> findByIdAndRoom_Id(Long id, Long roomId);

	boolean existsByRoom_IdAndFilePathIgnoreCase(Long roomId, String filePath);

	long countByRoom_Id(Long roomId);

	long countByRoom_IdIn(List<Long> roomIds);

	List<WorkspaceFile> findTop20ByRoom_IdInOrderByUpdatedAtDesc(List<Long> roomIds);
}
