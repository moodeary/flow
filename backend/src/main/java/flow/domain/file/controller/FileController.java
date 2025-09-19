package flow.domain.file.controller;

import flow.common.dto.ResponseApi;
import flow.common.exception.BusinessException;
import flow.domain.file.dto.FileResponse;
import flow.domain.file.entity.FileEntity;
import flow.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ResponseApi<FileResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileEntity uploadedFile = fileService.uploadFile(file);
            FileResponse response = FileResponse.from(uploadedFile);
            return ResponseEntity.ok(ResponseApi.success(response, "파일이 성공적으로 업로드되었습니다."));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(ResponseApi.error(e.getMessage(), e.getErrorCode()));
        }
    }

    @GetMapping
    public ResponseEntity<ResponseApi<List<FileResponse>>> getAllFiles() {
        List<FileEntity> files = fileService.getAllFiles();
        List<FileResponse> responses = files.stream()
                .map(FileResponse::from)
                .toList();
        String message = String.format("파일 %d개", files.size());
        return ResponseEntity.ok(ResponseApi.success(responses, message));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<FileResponse>> getFileById(@PathVariable Long id) {
        try {
            FileEntity file = fileService.getFileById(id);
            FileResponse response = FileResponse.from(file);
            return ResponseEntity.ok(ResponseApi.success(response));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(ResponseApi.error(e.getMessage(), e.getErrorCode()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            FileEntity file = fileService.getFileById(id);
            Resource resource = fileService.downloadFile(id);

            // 파일명 인코딩 (한글 파일명 지원)
            String encodedFilename = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getOriginalFilename() + "\"; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (BusinessException e) {
            log.error("파일 다운로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getStatus()).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseApi<Void>> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return ResponseEntity.ok(ResponseApi.success(null, "파일이 성공적으로 삭제되었습니다."));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(ResponseApi.error(e.getMessage(), e.getErrorCode()));
        }
    }

    @PostMapping("/initialize")
    public ResponseEntity<ResponseApi<String>> initializeUploadDirectory() {
        fileService.initializeUploadDirectory();
        return ResponseEntity.ok(ResponseApi.success("업로드 디렉토리가 초기화되었습니다."));
    }
}