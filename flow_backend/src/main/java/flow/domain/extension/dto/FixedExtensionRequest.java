package flow.domain.extension.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FixedExtensionRequest {

    @NotBlank(message = "확장자를 입력해주세요.")
    @Size(max = 20, message = "확장자는 최대 20자까지 입력 가능합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "확장자는 영문자와 숫자만 입력 가능합니다.")
    private String extension;

    @Size(max = 100, message = "설명은 최대 100자까지 입력 가능합니다.")
    private String description;

    private Boolean isBlocked;
}