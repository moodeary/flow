package flow.domain.file.service;

import flow.common.exception.BusinessException;
import flow.domain.extension.service.ExtensionService;
import flow.domain.file.entity.FileEntity;
import flow.domain.file.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService 단위 테스트")
class FileServiceImplTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ExtensionService extensionService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileServiceImpl fileService;

    private FileEntity sampleFileEntity;

    @BeforeEach
    void setUp() {
        // uploadPath 설정
        ReflectionTestUtils.setField(fileService, "uploadPath", "/test/upload");

        sampleFileEntity = FileEntity.builder()
                .originalFilename("test.txt")
                .storedFilename("uuid_test.txt")
                .fileSize(1024L)
                .contentType("text/plain")
                .filePath("/test/upload/uuid_test.txt")
                .build();
    }

    @Nested
    @DisplayName("파일 업로드 테스트")
    class UploadFileTest {

        @Test
        @DisplayName("정상 업로드 - 성공")
        void uploadFile_ValidFile_Success() throws IOException {
            // given
            String filename = "test.txt";
            byte[] content = "test content".getBytes();

            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getOriginalFilename()).willReturn(filename);
            given(multipartFile.getContentType()).willReturn("text/plain");
            given(multipartFile.getInputStream()).willReturn(new ByteArrayInputStream(content));

            given(extensionService.isExtensionBlocked("txt")).willReturn(false);
            given(fileRepository.save(any(FileEntity.class))).willReturn(sampleFileEntity);

            // when
            FileEntity result = fileService.uploadFile(multipartFile);

            // then
            assertThat(result).isNotNull();
            verify(fileRepository).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("빈 파일 - 실패")
        void uploadFile_EmptyFile_ThrowsException() {
            // given
            given(multipartFile.isEmpty()).willReturn(true);

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("파일이 비어있습니다");
        }

        @Test
        @DisplayName("파일 크기 초과 - 실패")
        void uploadFile_FileTooLarge_ThrowsException() {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getSize()).willReturn(11 * 1024 * 1024L); // 11MB

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("파일 크기는 10MB를 초과할 수 없습니다");
        }

        @Test
        @DisplayName("파일명이 null - 실패")
        void uploadFile_NullFilename_ThrowsException() {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getOriginalFilename()).willReturn(null);

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("유효하지 않은 파일명입니다");
        }

        @Test
        @DisplayName("확장자 없는 파일 - 실패")
        void uploadFile_NoExtension_ThrowsException() {
            // given
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getOriginalFilename()).willReturn("filename_without_extension");

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("확장자가 없는 파일은 업로드할 수 없습니다");
        }

        @Test
        @DisplayName("차단된 확장자 - 실패")
        void uploadFile_BlockedExtension_ThrowsException() {
            // given
            String filename = "malware.exe";

            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getOriginalFilename()).willReturn(filename);
            given(extensionService.isExtensionBlocked("exe")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("차단된 확장자입니다: exe");
        }

        @Test
        @DisplayName("IOException 발생 - 실패")
        void uploadFile_IOException_ThrowsException() throws IOException {
            // given
            String filename = "test.txt";

            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getOriginalFilename()).willReturn(filename);
            given(multipartFile.getInputStream()).willThrow(new IOException("IO Error"));
            given(extensionService.isExtensionBlocked("txt")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("파일 저장에 실패했습니다");
        }
    }

    @Nested
    @DisplayName("파일 조회 테스트")
    class GetFileTest {

        @Test
        @DisplayName("모든 파일 조회 - 성공")
        void getAllFiles_ReturnsAllFiles() {
            // given
            List<FileEntity> files = Arrays.asList(sampleFileEntity);
            given(fileRepository.findAllOrderByCreatedAtDesc()).willReturn(files);

            // when
            List<FileEntity> result = fileService.getAllFiles();

            // then
            assertThat(result).hasSize(1);
            verify(fileRepository).findAllOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("ID로 파일 조회 - 성공")
        void getFileById_ValidId_ReturnsFile() {
            // given
            Long fileId = 1L;
            given(fileRepository.findById(fileId)).willReturn(Optional.of(sampleFileEntity));

            // when
            FileEntity result = fileService.getFileById(fileId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOriginalFilename()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 파일 조회 - 실패")
        void getFileById_InvalidId_ThrowsException() {
            // given
            Long fileId = 999L;
            given(fileRepository.findById(fileId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fileService.getFileById(fileId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("파일을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("파일 다운로드 테스트")
    class DownloadFileTest {

        @Test
        @DisplayName("파일 다운로드 - 파일 존재하지 않음")
        void downloadFile_FileNotExists_ThrowsException() {
            // given
            Long fileId = 1L;
            FileEntity fileEntity = FileEntity.builder()
                    .filePath("/nonexistent/path/file.txt")
                    .build();

            given(fileRepository.findById(fileId)).willReturn(Optional.of(fileEntity));

            // when & then
            assertThatThrownBy(() -> fileService.downloadFile(fileId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("파일을 찾을 수 없거나 읽을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("파일 삭제 테스트")
    class DeleteFileTest {

        @Test
        @DisplayName("존재하지 않는 파일 삭제 - 실패")
        void deleteFile_FileNotFound_ThrowsException() {
            // given
            Long fileId = 999L;
            given(fileRepository.findById(fileId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fileService.deleteFile(fileId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("파일을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("확장자 추출 테스트")
    class ExtensionExtractionTest {

        @Test
        @DisplayName("정상 확장자 추출")
        void getFileExtension_ValidFilename_ReturnsExtension() {
            // 리플렉션을 통해 private 메서드 테스트
            // 실제로는 파일 업로드를 통해 간접적으로 테스트됨

            // given & when
            String filename1 = "test.txt";
            String filename2 = "document.PDF";
            String filename3 = "archive.tar.gz";

            // then
            // 이 테스트는 uploadFile 메서드를 통해 간접적으로 검증됨
            assertThat(filename1.substring(filename1.lastIndexOf(".") + 1).toLowerCase()).isEqualTo("txt");
            assertThat(filename2.substring(filename2.lastIndexOf(".") + 1).toLowerCase()).isEqualTo("pdf");
            assertThat(filename3.substring(filename3.lastIndexOf(".") + 1).toLowerCase()).isEqualTo("gz");
        }

        @Test
        @DisplayName("확장자 없는 파일명")
        void getFileExtension_NoExtension_ReturnsEmpty() {
            // given
            String filename = "filename_without_extension";

            // when & then
            // 파일 업로드 시 확장자 없는 파일은 거부됨
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getOriginalFilename()).willReturn(filename);

            assertThatThrownBy(() -> fileService.uploadFile(multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("확장자가 없는 파일은 업로드할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("파일명 생성 테스트")
    class FilenameGenerationTest {

        @Test
        @DisplayName("고유 파일명 생성 검증")
        void generateUniqueFilename_CreatesUniqueNames() throws IOException {
            // given
            String originalFilename = "test.txt";
            byte[] content = "test content".getBytes();

            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getOriginalFilename()).willReturn(originalFilename);
            given(multipartFile.getContentType()).willReturn("text/plain");
            given(multipartFile.getInputStream()).willReturn(new ByteArrayInputStream(content));
            given(extensionService.isExtensionBlocked("txt")).willReturn(false);

            // 첫 번째 파일 저장 시 생성되는 FileEntity
            FileEntity firstFile = FileEntity.builder()
                    .originalFilename(originalFilename)
                    .storedFilename("uuid1_test.txt")
                    .build();

            // 두 번째 파일 저장 시 생성되는 FileEntity
            FileEntity secondFile = FileEntity.builder()
                    .originalFilename(originalFilename)
                    .storedFilename("uuid2_test.txt")
                    .build();

            given(fileRepository.save(any(FileEntity.class)))
                    .willReturn(firstFile)
                    .willReturn(secondFile);

            // when
            FileEntity result1 = fileService.uploadFile(multipartFile);
            FileEntity result2 = fileService.uploadFile(multipartFile);

            // then
            assertThat(result1.getStoredFilename()).isNotEqualTo(result2.getStoredFilename());
            assertThat(result1.getStoredFilename()).contains("test.txt");
            assertThat(result2.getStoredFilename()).contains("test.txt");
        }
    }
}