package flow.config;

import flow.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FileUploadConfig {

    private final FileService fileService;

    @Bean
    public ApplicationRunner initializeFileUpload() {
        return args -> {
            try {
                fileService.initializeUploadDirectory();
                log.info("파일 업로드 디렉토리 초기화 완료");
            } catch (Exception e) {
                log.error("파일 업로드 디렉토리 초기화 실패", e);
            }
        };
    }
}