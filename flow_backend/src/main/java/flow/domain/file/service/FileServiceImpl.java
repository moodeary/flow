package flow.domain.file.service;

import flow.common.exception.BusinessException;
import flow.domain.extension.service.ExtensionService;
import flow.domain.file.entity.FileEntity;
import flow.domain.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final ExtensionService extensionService;

    @Value("${app.upload.path:/flow/data}")
    private String uploadPath;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public FileEntity uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw BusinessException.badRequest("유효하지 않은 파일명입니다.");
        }

        // 확장자 추출 및 차단 여부 확인
        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty()) {
            throw BusinessException.badRequest("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        if (extensionService.isExtensionBlocked(extension)) {
            throw BusinessException.badRequest("차단된 확장자입니다: " + extension);
        }

        // 파일 저장
        String storedFilename = generateUniqueFilename(originalFilename);
        Path uploadDir = Paths.get(uploadPath);
        Path filePath = uploadDir.resolve(storedFilename);

        try {
            // 업로드 디렉토리가 없으면 생성
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 데이터베이스에 파일 정보 저장
            FileEntity fileEntity = FileEntity.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .filePath(filePath.toString())
                    .build();

            return fileRepository.save(fileEntity);

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage(), e);
            throw BusinessException.internalServerError("파일 저장에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAllOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public FileEntity getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("파일을 찾을 수 없습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(Long id) {
        FileEntity fileEntity = getFileById(id);

        try {
            Path filePath = Paths.get(fileEntity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw BusinessException.notFound("파일을 찾을 수 없거나 읽을 수 없습니다.");
            }
        } catch (MalformedURLException e) {
            log.error("파일 다운로드 실패: {}", e.getMessage(), e);
            throw BusinessException.internalServerError("파일 다운로드에 실패했습니다.");
        }
    }

    @Override
    public void deleteFile(Long id) {
        FileEntity fileEntity = getFileById(id);

        try {
            // 실제 파일 삭제
            Path filePath = Paths.get(fileEntity.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // 데이터베이스에서 파일 정보 삭제
            fileRepository.delete(fileEntity);

        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            throw BusinessException.internalServerError("파일 삭제에 실패했습니다.");
        }
    }

    @Override
    public void initializeUploadDirectory() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("업로드 디렉토리 생성: {}", uploadPath);
            }
        } catch (IOException e) {
            log.error("업로드 디렉토리 생성 실패: {}", e.getMessage(), e);
            throw BusinessException.internalServerError("업로드 디렉토리 생성에 실패했습니다.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String uuid = UUID.randomUUID().toString();
        String cleanFilename = StringUtils.cleanPath(originalFilename);

        // 파일명에서 확장자 제거
        if (dotIndex > 0) {
            cleanFilename = cleanFilename.substring(0, cleanFilename.lastIndexOf("."));
        }

        return uuid + "_" + cleanFilename + extension;
    }
}