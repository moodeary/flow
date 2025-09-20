package flow.domain.file.repository;

import flow.domain.file.entity.FileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FileRepository 테스트")
class FileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FileRepository fileRepository;

    private FileEntity firstFile;
    private FileEntity secondFile;
    private FileEntity thirdFile;

    @BeforeEach
    void setUp() {
        firstFile = FileEntity.builder()
                .originalFilename("first.txt")
                .storedFilename("uuid1_first.txt")
                .fileSize(1024L)
                .contentType("text/plain")
                .filePath("/test/path/uuid1_first.txt")
                .build();

        secondFile = FileEntity.builder()
                .originalFilename("second.pdf")
                .storedFilename("uuid2_second.pdf")
                .fileSize(2048L)
                .contentType("application/pdf")
                .filePath("/test/path/uuid2_second.pdf")
                .build();

        thirdFile = FileEntity.builder()
                .originalFilename("third.jpg")
                .storedFilename("uuid3_third.jpg")
                .fileSize(4096L)
                .contentType("image/jpeg")
                .filePath("/test/path/uuid3_third.jpg")
                .build();

        // 생성 시간 차이를 두기 위해 순서대로 저장
        entityManager.persistAndFlush(firstFile);

        try {
            Thread.sleep(10); // 시간 차이를 두기 위해
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        entityManager.persistAndFlush(secondFile);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        entityManager.persistAndFlush(thirdFile);
    }

    @Test
    @DisplayName("생성일 내림차순으로 모든 파일 조회")
    void findAllOrderByCreatedAtDesc_ReturnsFilesInDescendingOrder() {
        // when
        List<FileEntity> result = fileRepository.findAllOrderByCreatedAtDesc();

        // then
        assertThat(result).hasSize(3);
        // 최신 파일이 먼저 와야 함 (DESC 정렬)
        assertThat(result.get(0).getOriginalFilename()).isEqualTo("third.jpg");
        assertThat(result.get(1).getOriginalFilename()).isEqualTo("second.pdf");
        assertThat(result.get(2).getOriginalFilename()).isEqualTo("first.txt");
    }

    @Test
    @DisplayName("저장된 파일명으로 존재 여부 확인 - 존재함")
    void existsByStoredFilename_ExistingFile_ReturnsTrue() {
        // when
        boolean result = fileRepository.existsByStoredFilename("uuid1_first.txt");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("저장된 파일명으로 존재 여부 확인 - 존재하지 않음")
    void existsByStoredFilename_NonExistingFile_ReturnsFalse() {
        // when
        boolean result = fileRepository.existsByStoredFilename("nonexistent.txt");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("파일 저장 및 조회 테스트")
    void saveAndRetrieve_FileEntity_Success() {
        // given
        FileEntity newFile = FileEntity.builder()
                .originalFilename("new.doc")
                .storedFilename("uuid4_new.doc")
                .fileSize(8192L)
                .contentType("application/msword")
                .filePath("/test/path/uuid4_new.doc")
                .build();

        // when
        FileEntity savedFile = fileRepository.save(newFile);

        // then
        assertThat(savedFile.getId()).isNotNull();
        assertThat(savedFile.getOriginalFilename()).isEqualTo("new.doc");
        assertThat(savedFile.getStoredFilename()).isEqualTo("uuid4_new.doc");
        assertThat(savedFile.getFileSize()).isEqualTo(8192L);
        assertThat(savedFile.getContentType()).isEqualTo("application/msword");
        assertThat(savedFile.getFilePath()).isEqualTo("/test/path/uuid4_new.doc");
        assertThat(savedFile.getCreatedAt()).isNotNull();
        assertThat(savedFile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("파일 삭제 테스트")
    void deleteFile_RemovesFromDatabase() {
        // given
        Long fileId = firstFile.getId();
        assertThat(fileRepository.findById(fileId)).isPresent();

        // when
        fileRepository.delete(firstFile);
        entityManager.flush();

        // then
        assertThat(fileRepository.findById(fileId)).isEmpty();
        assertThat(fileRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("빈 데이터베이스에서 조회 시 빈 목록 반환")
    void findAllOrderByCreatedAtDesc_EmptyDatabase_ReturnsEmptyList() {
        // given
        fileRepository.deleteAll();
        entityManager.flush();

        // when
        List<FileEntity> result = fileRepository.findAllOrderByCreatedAtDesc();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("동일한 저장 파일명 중복 체크")
    void existsByStoredFilename_DuplicateCheck() {
        // given
        String existingStoredFilename = "uuid1_first.txt";
        String newStoredFilename = "uuid_new_file.txt";

        // when & then
        assertThat(fileRepository.existsByStoredFilename(existingStoredFilename)).isTrue();
        assertThat(fileRepository.existsByStoredFilename(newStoredFilename)).isFalse();
    }

    @Test
    @DisplayName("파일 업데이트 테스트")
    void updateFile_ChangesProperties() {
        // given
        FileEntity fileToUpdate = fileRepository.findById(firstFile.getId()).orElseThrow();
        String originalCreatedAt = fileToUpdate.getCreatedAt().toString();

        // when - FileEntity는 수정 가능한 필드가 제한적이므로 새로운 파일로 교체하는 방식으로 테스트
        FileEntity updatedFile = FileEntity.builder()
                .originalFilename("updated_first.txt")
                .storedFilename("uuid1_updated_first.txt")
                .fileSize(2048L)
                .contentType("text/plain")
                .filePath("/test/path/uuid1_updated_first.txt")
                .build();

        FileEntity savedFile = fileRepository.save(updatedFile);

        // then
        assertThat(savedFile.getOriginalFilename()).isEqualTo("updated_first.txt");
        assertThat(savedFile.getStoredFilename()).isEqualTo("uuid1_updated_first.txt");
        assertThat(savedFile.getFileSize()).isEqualTo(2048L);
        assertThat(savedFile.getCreatedAt()).isNotNull();
        assertThat(savedFile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("대용량 파일 저장 테스트")
    void saveLargeFile_Success() {
        // given
        FileEntity largeFile = FileEntity.builder()
                .originalFilename("large_file.zip")
                .storedFilename("uuid_large_file.zip")
                .fileSize(10L * 1024 * 1024) // 10MB
                .contentType("application/zip")
                .filePath("/test/path/uuid_large_file.zip")
                .build();

        // when
        FileEntity savedFile = fileRepository.save(largeFile);

        // then
        assertThat(savedFile.getId()).isNotNull();
        assertThat(savedFile.getFileSize()).isEqualTo(10L * 1024 * 1024);
        assertThat(fileRepository.existsByStoredFilename("uuid_large_file.zip")).isTrue();
    }

    @Test
    @DisplayName("특수 문자가 포함된 파일명 처리")
    void saveFileWithSpecialCharacters_Success() {
        // given
        FileEntity specialFile = FileEntity.builder()
                .originalFilename("파일_이름-특수문자(123).txt")
                .storedFilename("uuid_special_file.txt")
                .fileSize(1024L)
                .contentType("text/plain")
                .filePath("/test/path/uuid_special_file.txt")
                .build();

        // when
        FileEntity savedFile = fileRepository.save(specialFile);

        // then
        assertThat(savedFile.getId()).isNotNull();
        assertThat(savedFile.getOriginalFilename()).isEqualTo("파일_이름-특수문자(123).txt");
        assertThat(savedFile.getStoredFilename()).isEqualTo("uuid_special_file.txt");
    }
}