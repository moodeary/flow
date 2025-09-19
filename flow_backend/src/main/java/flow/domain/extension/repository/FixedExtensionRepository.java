package flow.domain.extension.repository;
import flow.domain.extension.entity.FixedExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FixedExtensionRepository extends JpaRepository<FixedExtension, Long> {

    Optional<FixedExtension> findByExtension(String extension);

    List<FixedExtension> findAllByIsBlocked(boolean isBlocked);

    @Query("SELECT f FROM FixedExtension f ORDER BY f.extension ASC")
    List<FixedExtension> findAllOrderByExtension();

    boolean existsByExtension(String extension);

    @Query("SELECT f.extension FROM FixedExtension f WHERE f.isBlocked = true")
    List<String> findBlockedExtensions();

}