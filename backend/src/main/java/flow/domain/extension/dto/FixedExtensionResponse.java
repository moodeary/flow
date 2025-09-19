package flow.domain.extension.dto;

import flow.domain.extension.entity.FixedExtension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixedExtensionResponse {
    private Long id;
    private String extension;
    private boolean isBlocked;
    private String description;

    public static FixedExtensionResponse from(FixedExtension fixedExtension) {
        return FixedExtensionResponse.builder()
                .id(fixedExtension.getId())
                .extension(fixedExtension.getExtension())
                .isBlocked(fixedExtension.isBlocked())
                .description(fixedExtension.getDescription())
                .build();
    }
}