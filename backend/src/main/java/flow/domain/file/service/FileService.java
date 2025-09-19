package flow.domain.file.service;

import flow.domain.file.entity.FileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    FileEntity uploadFile(MultipartFile file);

    List<FileEntity> getAllFiles();

    FileEntity getFileById(Long id);

    Resource downloadFile(Long id);

    void deleteFile(Long id);

    void initializeUploadDirectory();
}