package flow.domain.extension.service;

import flow.common.exception.BusinessException;
import flow.domain.extension.entity.CustomExtension;
import flow.domain.extension.entity.FixedExtension;
import flow.domain.extension.repository.CustomExtensionRepository;
import flow.domain.extension.repository.FixedExtensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExtensionService 단위 테스트")
class ExtensionServiceImplTest {

    @Mock
    private FixedExtensionRepository fixedExtensionRepository;

    @Mock
    private CustomExtensionRepository customExtensionRepository;

    @InjectMocks
    private ExtensionServiceImpl extensionService;

    private FixedExtension sampleFixedExtension;
    private CustomExtension sampleCustomExtension;

    @BeforeEach
    void setUp() {
        sampleFixedExtension = FixedExtension.builder()
                .extension("exe")
                .isBlocked(false)
                .description("실행 파일")
                .build();

        sampleCustomExtension = CustomExtension.builder()
                .extension("test")
                .build();
    }

    @Nested
    @DisplayName("확장자 유효성 검증 테스트")
    class ValidateExtensionTest {

        @Test
        @DisplayName("유효한 확장자 - 성공")
        void validateExtension_ValidExtension_ReturnsTrue() {
            // given & when & then
            assertThat(extensionService.validateExtension("exe")).isTrue();
            assertThat(extensionService.validateExtension("PDF")).isTrue();
            assertThat(extensionService.validateExtension("txt123")).isTrue();
        }

        @Test
        @DisplayName("null 또는 빈 문자열 - 실패")
        void validateExtension_NullOrEmpty_ReturnsFalse() {
            // given & when & then
            assertThat(extensionService.validateExtension(null)).isFalse();
            assertThat(extensionService.validateExtension("")).isFalse();
            assertThat(extensionService.validateExtension("   ")).isFalse();
        }

        @Test
        @DisplayName("길이 초과 - 실패")
        void validateExtension_TooLong_ReturnsFalse() {
            // given
            String longExtension = "a".repeat(21);

            // when & then
            assertThat(extensionService.validateExtension(longExtension)).isFalse();
        }

        @Test
        @DisplayName("특수문자 포함 - 실패")
        void validateExtension_WithSpecialCharacters_ReturnsFalse() {
            // given & when & then
            assertThat(extensionService.validateExtension("exe.txt")).isFalse();
            assertThat(extensionService.validateExtension("exe-txt")).isFalse();
            assertThat(extensionService.validateExtension("exe_txt")).isFalse();
            assertThat(extensionService.validateExtension("exe@txt")).isFalse();
        }
    }

    @Nested
    @DisplayName("고정 확장자 추가 테스트")
    class AddFixedExtensionTest {

        @Test
        @DisplayName("정상 추가 - 성공")
        void addFixedExtension_Valid_Success() {
            // given
            String extension = "pdf";
            String description = "PDF 파일";

            given(fixedExtensionRepository.findAll()).willReturn(Arrays.asList(sampleFixedExtension));
            given(fixedExtensionRepository.existsByExtension(extension)).willReturn(false);
            given(customExtensionRepository.existsByExtension(extension)).willReturn(false);
            given(fixedExtensionRepository.save(any(FixedExtension.class))).willReturn(sampleFixedExtension);

            // when
            FixedExtension result = extensionService.addFixedExtension(extension, description);

            // then
            assertThat(result).isNotNull();
            verify(fixedExtensionRepository).save(any(FixedExtension.class));
        }

        @Test
        @DisplayName("유효하지 않은 확장자 - 실패")
        void addFixedExtension_InvalidExtension_ThrowsException() {
            // given
            String invalidExtension = "pdf.exe";

            // when & then
            assertThatThrownBy(() -> extensionService.addFixedExtension(invalidExtension, "설명"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("유효하지 않은 확장자입니다");
        }

        @Test
        @DisplayName("최대 개수 초과 - 실패")
        void addFixedExtension_ExceedsMaxCount_ThrowsException() {
            // given
            List<FixedExtension> maxExtensions = Arrays.asList(
                    new FixedExtension[10] // MAX_FIXED_EXTENSIONS = 10
            );
            given(fixedExtensionRepository.findAll()).willReturn(maxExtensions);

            // when & then
            assertThatThrownBy(() -> extensionService.addFixedExtension("pdf", "설명"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("고정 확장자는 최대 10개까지");
        }

        @Test
        @DisplayName("고정 확장자 중복 - 실패")
        void addFixedExtension_DuplicateInFixed_ThrowsException() {
            // given
            String extension = "exe";
            given(fixedExtensionRepository.findAll()).willReturn(Arrays.asList(sampleFixedExtension));
            given(fixedExtensionRepository.existsByExtension(extension)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> extensionService.addFixedExtension(extension, "설명"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 존재하는 고정 확장자입니다");
        }

        @Test
        @DisplayName("커스텀 확장자와 중복 - 실패")
        void addFixedExtension_DuplicateInCustom_ThrowsException() {
            // given
            String extension = "test";
            given(fixedExtensionRepository.findAll()).willReturn(Arrays.asList(sampleFixedExtension));
            given(fixedExtensionRepository.existsByExtension(extension)).willReturn(false);
            given(customExtensionRepository.existsByExtension(extension)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> extensionService.addFixedExtension(extension, "설명"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 커스텀 확장자에 존재합니다");
        }
    }

    @Nested
    @DisplayName("커스텀 확장자 추가 테스트")
    class AddCustomExtensionTest {

        @Test
        @DisplayName("정상 추가 - 성공")
        void addCustomExtension_Valid_Success() {
            // given
            String extension = "pdf";

            given(customExtensionRepository.findAll()).willReturn(Arrays.asList(sampleCustomExtension));
            given(fixedExtensionRepository.existsByExtension(extension)).willReturn(false);
            given(customExtensionRepository.existsByExtension(extension)).willReturn(false);
            given(customExtensionRepository.save(any(CustomExtension.class))).willReturn(sampleCustomExtension);

            // when
            CustomExtension result = extensionService.addCustomExtension(extension);

            // then
            assertThat(result).isNotNull();
            verify(customExtensionRepository).save(any(CustomExtension.class));
        }

        @Test
        @DisplayName("유효하지 않은 확장자 - 실패")
        void addCustomExtension_InvalidExtension_ThrowsException() {
            // given
            String invalidExtension = "pdf.exe";

            // when & then
            assertThatThrownBy(() -> extensionService.addCustomExtension(invalidExtension))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("유효하지 않은 확장자입니다");
        }

        @Test
        @DisplayName("최대 개수 초과 - 실패")
        void addCustomExtension_ExceedsMaxCount_ThrowsException() {
            // given
            List<CustomExtension> maxExtensions = Arrays.asList(
                    new CustomExtension[200] // MAX_CUSTOM_EXTENSIONS = 200
            );
            given(customExtensionRepository.findAll()).willReturn(maxExtensions);

            // when & then
            assertThatThrownBy(() -> extensionService.addCustomExtension("pdf"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("커스텀 확장자는 최대 200개까지");
        }
    }

    @Nested
    @DisplayName("확장자 차단 여부 확인 테스트")
    class IsExtensionBlockedTest {

        @Test
        @DisplayName("고정 확장자가 차단됨 - true")
        void isExtensionBlocked_FixedExtensionBlocked_ReturnsTrue() {
            // given
            String extension = "exe";
            FixedExtension blockedFixed = FixedExtension.builder()
                    .extension(extension)
                    .isBlocked(true)
                    .description("실행 파일")
                    .build();

            given(fixedExtensionRepository.findByExtension(extension)).willReturn(Optional.of(blockedFixed));
            given(customExtensionRepository.findByExtension(extension)).willReturn(Optional.empty());

            // when
            boolean result = extensionService.isExtensionBlocked(extension);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("커스텀 확장자가 차단됨 - true")
        void isExtensionBlocked_CustomExtensionBlocked_ReturnsTrue() {
            // given
            String extension = "test";
            CustomExtension blockedCustom = CustomExtension.builder()
                    .extension(extension)
                    .build(); // CustomExtension은 기본적으로 isBlocked = true

            given(fixedExtensionRepository.findByExtension(extension)).willReturn(Optional.empty());
            given(customExtensionRepository.findByExtension(extension)).willReturn(Optional.of(blockedCustom));

            // when
            boolean result = extensionService.isExtensionBlocked(extension);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("확장자가 존재하지 않음 - false")
        void isExtensionBlocked_ExtensionNotExists_ReturnsFalse() {
            // given
            String extension = "unknown";

            given(fixedExtensionRepository.findByExtension(extension)).willReturn(Optional.empty());
            given(customExtensionRepository.findByExtension(extension)).willReturn(Optional.empty());

            // when
            boolean result = extensionService.isExtensionBlocked(extension);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("고정 확장자가 차단되지 않음 - false")
        void isExtensionBlocked_FixedExtensionNotBlocked_ReturnsFalse() {
            // given
            String extension = "exe";
            FixedExtension unblocked = FixedExtension.builder()
                    .extension(extension)
                    .isBlocked(false)
                    .description("실행 파일")
                    .build();

            given(fixedExtensionRepository.findByExtension(extension)).willReturn(Optional.of(unblocked));
            given(customExtensionRepository.findByExtension(extension)).willReturn(Optional.empty());

            // when
            boolean result = extensionService.isExtensionBlocked(extension);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("고정 확장자 상태 업데이트 테스트")
    class UpdateFixedExtensionStatusTest {

        @Test
        @DisplayName("정상 업데이트 - 성공")
        void updateFixedExtensionStatus_ValidExtension_Success() {
            // given
            String extension = "exe";
            boolean newStatus = true;

            given(fixedExtensionRepository.findByExtension(extension)).willReturn(Optional.of(sampleFixedExtension));
            given(fixedExtensionRepository.save(any(FixedExtension.class))).willReturn(sampleFixedExtension);

            // when
            FixedExtension result = extensionService.updateFixedExtensionStatus(extension, newStatus);

            // then
            assertThat(result).isNotNull();
            verify(fixedExtensionRepository).save(any(FixedExtension.class));
        }

        @Test
        @DisplayName("존재하지 않는 확장자 - 실패")
        void updateFixedExtensionStatus_ExtensionNotFound_ThrowsException() {
            // given
            String extension = "unknown";

            given(fixedExtensionRepository.findByExtension(extension)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> extensionService.updateFixedExtensionStatus(extension, true))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("고정 확장자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("조회 메서드 테스트")
    class GetMethodsTest {

        @Test
        @DisplayName("모든 고정 확장자 조회")
        void getAllFixedExtensions_ReturnsAll() {
            // given
            List<FixedExtension> extensions = Arrays.asList(sampleFixedExtension);
            given(fixedExtensionRepository.findAllOrderByExtension()).willReturn(extensions);

            // when
            List<FixedExtension> result = extensionService.getAllFixedExtensions();

            // then
            assertThat(result).hasSize(1);
            verify(fixedExtensionRepository).findAllOrderByExtension();
        }

        @Test
        @DisplayName("모든 커스텀 확장자 조회")
        void getAllCustomExtensions_ReturnsAll() {
            // given
            List<CustomExtension> extensions = Arrays.asList(sampleCustomExtension);
            given(customExtensionRepository.findAllOrderByCreatedAt()).willReturn(extensions);

            // when
            List<CustomExtension> result = extensionService.getAllCustomExtensions();

            // then
            assertThat(result).hasSize(1);
            verify(customExtensionRepository).findAllOrderByCreatedAt();
        }
    }
}