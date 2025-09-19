package flow.domain.extension.entity;

import flow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_extensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomExtension extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "extension", nullable = false, unique = true, length = 20)
    private String extension;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = true;

    @Builder
    public CustomExtension(String extension) {
        this.extension = extension.toLowerCase();
        this.isBlocked = true;
    }

    public void updateBlockStatus(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}