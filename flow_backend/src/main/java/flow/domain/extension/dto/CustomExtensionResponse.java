package flow.domain.extension.dto;


import flow.domain.extension.entity.CustomExtension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomExtensionResponse {
    private Long id;
    private String extension;

    public static CustomExtensionResponse from(CustomExtension customExtension) {
        return CustomExtensionResponse.builder()
                .id(customExtension.getId())
                .extension(customExtension.getExtension())
                .build();
    }
}