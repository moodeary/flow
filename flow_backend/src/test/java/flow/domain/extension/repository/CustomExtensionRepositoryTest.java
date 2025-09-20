package flow.domain.extension.repository;

import flow.domain.extension.entity.CustomExtension;
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
@DisplayName("CustomExtensionRepository 테스트")
class CustomExtensionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomExtensionRepository customExtensionRepository;

    private CustomExtension firstExtension;
    private CustomExtension secondExtension;

    @BeforeEach
    void setUp() {
        firstExtension = CustomExtension.builder()
                .extension("custom1")
                .build();

        secondExtension = CustomExtension.builder()
                .extension("custom2")
                .build();

        // CustomExtension은 기본적으로 isBlocked = true
        entityManager.persistAndFlush(firstExtension);

        // 잠시 기다린 후 두 번째 생성 (createdAt 순서를 위해)
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        entityManager.persistAndFlush(secondExtension);
    }

    @Test
    @DisplayName("확장자로 커스텀 확장자 찾기")
    void findByExtension_ExistingExtension_ReturnsExtension() {
        // when
        Optional<CustomExtension> result = customExtensionRepository.findByExtension("custom1");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getExtension()).isEqualTo("custom1");
        assertThat(result.get().isBlocked()).isTrue(); // 기본값
    }

    @Test
    @DisplayName("존재하지 않는 확장자 찾기")
    void findByExtension_NonExistingExtension_ReturnsEmpty() {
        // when
        Optional<CustomExtension> result = customExtensionRepository.findByExtension("nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("차단 상태별 커스텀 확장자 찾기 - 차단됨")
    void findAllByIsBlocked_Blocked_ReturnsBlockedExtensions() {
        // when
        List<CustomExtension> result = customExtensionRepository.findAllByIsBlocked(true);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CustomExtension::getExtension)
                .containsExactlyInAnyOrder("custom1", "custom2");
    }

    @Test
    @DisplayName("차단 상태별 커스텀 확장자 찾기 - 차단되지 않음")
    void findAllByIsBlocked_NotBlocked_ReturnsUnblockedExtensions() {
        // given - 하나의 확장자를 차단 해제
        firstExtension.updateBlockStatus(false);
        entityManager.persistAndFlush(firstExtension);

        // when
        List<CustomExtension> result = customExtensionRepository.findAllByIsBlocked(false);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExtension()).isEqualTo("custom1");
        assertThat(result.get(0).isBlocked()).isFalse();
    }

    @Test
    @DisplayName("생성일 순으로 모든 커스텀 확장자 조회")
    void findAllOrderByCreatedAt_ReturnsOrderedExtensions() {
        // when
        List<CustomExtension> result = customExtensionRepository.findAllOrderByCreatedAt();

        // then
        assertThat(result).hasSize(2);
        // 먼저 생성된 것이 첫 번째로 와야 함 (ASC 정렬)
        assertThat(result.get(0).getExtension()).isEqualTo("custom1");
        assertThat(result.get(1).getExtension()).isEqualTo("custom2");
    }

    @Test
    @DisplayName("확장자 존재 여부 확인 - 존재함")
    void existsByExtension_ExistingExtension_ReturnsTrue() {
        // when
        boolean result = customExtensionRepository.existsByExtension("custom1");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("확장자 존재 여부 확인 - 존재하지 않음")
    void existsByExtension_NonExistingExtension_ReturnsFalse() {
        // when
        boolean result = customExtensionRepository.existsByExtension("nonexistent");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("차단된 확장자 목록 조회")
    void findBlockedExtensions_ReturnsBlockedExtensionNames() {
        // when
        List<String> result = customExtensionRepository.findBlockedExtensions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("custom1", "custom2");
    }

    @Test
    @DisplayName("차단 해제된 확장자가 있을 때 차단된 것만 조회")
    void findBlockedExtensions_WithUnblockedExtension_ReturnsOnlyBlocked() {
        // given - 하나의 확장자를 차단 해제
        firstExtension.updateBlockStatus(false);
        entityManager.persistAndFlush(firstExtension);

        // when
        List<String> result = customExtensionRepository.findBlockedExtensions();

        // then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("custom2");
    }

    @Test
    @DisplayName("모든 확장자가 차단 해제되었을 때 빈 목록 반환")
    void findBlockedExtensions_AllUnblocked_ReturnsEmptyList() {
        // given - 모든 확장자를 차단 해제
        firstExtension.updateBlockStatus(false);
        secondExtension.updateBlockStatus(false);
        entityManager.persistAndFlush(firstExtension);
        entityManager.persistAndFlush(secondExtension);

        // when
        List<String> result = customExtensionRepository.findBlockedExtensions();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("확장자 상태 업데이트 테스트")
    void updateBlockStatus_ChangesStatus() {
        // given
        CustomExtension extension = customExtensionRepository.findByExtension("custom1").orElseThrow();
        assertThat(extension.isBlocked()).isTrue(); // 기본값

        // when
        extension.updateBlockStatus(false);
        CustomExtension savedExtension = customExtensionRepository.save(extension);

        // then
        assertThat(savedExtension.isBlocked()).isFalse();

        // 데이터베이스에서 다시 조회하여 확인
        CustomExtension reloadedExtension = customExtensionRepository.findByExtension("custom1").orElseThrow();
        assertThat(reloadedExtension.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("커스텀 확장자 기본 상태 확인")
    void customExtension_DefaultBlocked_IsTrue() {
        // given
        CustomExtension newExtension = CustomExtension.builder()
                .extension("newcustom")
                .build();

        // when
        CustomExtension savedExtension = customExtensionRepository.save(newExtension);

        // then
        assertThat(savedExtension.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("대량 데이터 처리 시 정렬 순서 확인")
    void findAllOrderByCreatedAt_WithManyExtensions_MaintainsOrder() {
        // given - 추가 확장자들 생성
        for (int i = 3; i <= 5; i++) {
            CustomExtension extension = CustomExtension.builder()
                    .extension("custom" + i)
                    .build();
            entityManager.persistAndFlush(extension);

            // 시간 차이를 두기 위해
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // when
        List<CustomExtension> result = customExtensionRepository.findAllOrderByCreatedAt();

        // then
        assertThat(result).hasSize(5);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getExtension()).isEqualTo("custom" + (i + 1));
        }
    }
}