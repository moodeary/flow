package flow.domain.file.repository;

import flow.domain.file.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query("SELECT f FROM FileEntity f ORDER BY f.createdAt DESC")
    List<FileEntity> findAllOrderByCreatedAtDesc();

    boolean existsByStoredFilename(String storedFilename);
}