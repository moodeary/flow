package flow.domain.extension.repository;
import flow.domain.extension.entity.CustomExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomExtensionRepository extends JpaRepository<CustomExtension, Long> {

    Optional<CustomExtension> findByExtension(String extension);

    List<CustomExtension> findAllByIsBlocked(boolean isBlocked);

    @Query("SELECT c FROM CustomExtension c ORDER BY c.createdAt ASC")
    List<CustomExtension> findAllOrderByCreatedAt();

    boolean existsByExtension(String extension);

    @Query("SELECT c.extension FROM CustomExtension c WHERE c.isBlocked = true")
    List<String> findBlockedExtensions();

}