package flow.domain.file.dto;

import flow.domain.file.entity.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {

    private Long id;
    private String originalFilename;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FileResponse from(FileEntity fileEntity) {
        return FileResponse.builder()
                .id(fileEntity.getId())
                .originalFilename(fileEntity.getOriginalFilename())
                .fileSize(fileEntity.getFileSize())
                .contentType(fileEntity.getContentType())
                .createdAt(fileEntity.getCreatedAt())
                .updatedAt(fileEntity.getUpdatedAt())
                .build();
    }
}