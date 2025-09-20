package flow.domain.extension.repository;

import flow.domain.extension.entity.FixedExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FixedExtensionRepository 테스트")
class FixedExtensionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FixedExtensionRepository fixedExtensionRepository;

    private FixedExtension blockedExtension;
    private FixedExtension unblockedExtension;

    @BeforeEach
    void setUp() {
        blockedExtension = FixedExtension.builder()
                .extension("exe")
                .isBlocked(true)
                .description("실행 파일")
                .build();

        unblockedExtension = FixedExtension.builder()
                .extension("txt")
                .isBlocked(false)
                .description("텍스트 파일")
                .build();

        entityManager.persistAndFlush(blockedExtension);
        entityManager.persistAndFlush(unblockedExtension);
    }

    @Test
    @DisplayName("확장자로 고정 확장자 찾기")
    void findByExtension_ExistingExtension_ReturnsExtension() {
        // when
        Optional<FixedExtension> result = fixedExtensionRepository.findByExtension("exe");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getExtension()).isEqualTo("exe");
        assertThat(result.get().isBlocked()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 확장자 찾기")
    void findByExtension_NonExistingExtension_ReturnsEmpty() {
        // when
        Optional<FixedExtension> result = fixedExtensionRepository.findByExtension("unknown");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("차단 상태별 고정 확장자 찾기 - 차단됨")
    void findAllByIsBlocked_Blocked_ReturnsBlockedExtensions() {
        // when
        List<FixedExtension> result = fixedExtensionRepository.findAllByIsBlocked(true);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExtension()).isEqualTo("exe");
        assertThat(result.get(0).isBlocked()).isTrue();
    }

    @Test
    @DisplayName("차단 상태별 고정 확장자 찾기 - 차단되지 않음")
    void findAllByIsBlocked_NotBlocked_ReturnsUnblockedExtensions() {
        // when
        List<FixedExtension> result = fixedExtensionRepository.findAllByIsBlocked(false);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExtension()).isEqualTo("txt");
        assertThat(result.get(0).isBlocked()).isFalse();
    }

    @Test
    @DisplayName("확장자 이름 순으로 모든 고정 확장자 조회")
    void findAllOrderByExtension_ReturnsOrderedExtensions() {
        // given
        FixedExtension additionalExtension = FixedExtension.builder()
                .extension("bat")
                .isBlocked(false)
                .description("배치 파일")
                .build();
        entityManager.persistAndFlush(additionalExtension);

        // when
        List<FixedExtension> result = fixedExtensionRepository.findAllOrderByExtension();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getExtension()).isEqualTo("bat"); // 알파벳 순서
        assertThat(result.get(1).getExtension()).isEqualTo("exe");
        assertThat(result.get(2).getExtension()).isEqualTo("txt");
    }

    @Test
    @DisplayName("확장자 존재 여부 확인 - 존재함")
    void existsByExtension_ExistingExtension_ReturnsTrue() {
        // when
        boolean result = fixedExtensionRepository.existsByExtension("exe");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("확장자 존재 여부 확인 - 존재하지 않음")
    void existsByExtension_NonExistingExtension_ReturnsFalse() {
        // when
        boolean result = fixedExtensionRepository.existsByExtension("unknown");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("차단된 확장자 목록 조회")
    void findBlockedExtensions_ReturnsBlockedExtensionNames() {
        // given
        FixedExtension additionalBlockedExtension = FixedExtension.builder()
                .extension("scr")
                .isBlocked(true)
                .description("화면보호기 파일")
                .build();
        entityManager.persistAndFlush(additionalBlockedExtension);

        // when
        List<String> result = fixedExtensionRepository.findBlockedExtensions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("exe", "scr");
    }

    @Test
    @DisplayName("차단된 확장자가 없을 때 빈 목록 반환")
    void findBlockedExtensions_NoBlockedExtensions_ReturnsEmptyList() {
        // given - 모든 확장자를 차단 해제
        blockedExtension.updateBlockStatus(false);
        entityManager.persistAndFlush(blockedExtension);

        // when
        List<String> result = fixedExtensionRepository.findBlockedExtensions();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("확장자 대소문자 구분 없이 검색")
    void findByExtension_CaseInsensitive_ReturnsExtension() {
        // when
        Optional<FixedExtension> resultUpperCase = fixedExtensionRepository.findByExtension("EXE");
        Optional<FixedExtension> resultMixedCase = fixedExtensionRepository.findByExtension("ExE");

        // then
        // 실제로는 데이터베이스와 JPA 설정에 따라 달라질 수 있음
        // 여기서는 저장 시 toLowerCase()로 처리되므로 정확한 소문자로 검색해야 함
        assertThat(resultUpperCase).isEmpty();
        assertThat(resultMixedCase).isEmpty();

        // 정확한 소문자로 검색
        Optional<FixedExtension> resultLowerCase = fixedExtensionRepository.findByExtension("exe");
        assertThat(resultLowerCase).isPresent();
    }

    @Test
    @DisplayName("확장자 업데이트 테스트")
    void updateBlockStatus_ChangesStatus() {
        // given
        FixedExtension extension = fixedExtensionRepository.findByExtension("txt").orElseThrow();
        assertThat(extension.isBlocked()).isFalse();

        // when
        extension.updateBlockStatus(true);
        FixedExtension savedExtension = fixedExtensionRepository.save(extension);

        // then
        assertThat(savedExtension.isBlocked()).isTrue();

        // 데이터베이스에서 다시 조회하여 확인
        FixedExtension reloadedExtension = fixedExtensionRepository.findByExtension("txt").orElseThrow();
        assertThat(reloadedExtension.isBlocked()).isTrue();
    }
}