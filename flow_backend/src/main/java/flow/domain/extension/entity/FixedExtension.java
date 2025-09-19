package flow.domain.extension.entity;

import flow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fixed_extensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedExtension extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "extension", nullable = false, unique = true, length = 20)
    private String extension;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    @Column(name = "description", length = 100)
    private String description;

    @Builder
    public FixedExtension(String extension, boolean isBlocked, String description) {
        this.extension = extension.toLowerCase();
        this.isBlocked = isBlocked;
        this.description = description;
    }

    public void updateBlockStatus(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}