package flow.domain.extension.service;

import flow.common.exception.BusinessException;
import flow.domain.extension.entity.CustomExtension;
import flow.domain.extension.repository.CustomExtensionRepository;
import flow.domain.extension.entity.FixedExtension;
import flow.domain.extension.repository.FixedExtensionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExtensionServiceImpl implements ExtensionService {

    private final FixedExtensionRepository fixedExtensionRepository;
    private final CustomExtensionRepository customExtensionRepository;

    private static final List<String> DEFAULT_FIXED_EXTENSIONS = Arrays.asList(
            "bat", "cmd", "com", "cpl", "exe", "scr", "js"
    );

    private static final int MAX_CUSTOM_EXTENSIONS = 200;
    private static final int MAX_FIXED_EXTENSIONS = 10;
    private static final int MAX_EXTENSION_LENGTH = 20;

    @Override
    @Transactional(readOnly = true)
    public List<FixedExtension> getAllFixedExtensions() {
        return fixedExtensionRepository.findAllOrderByExtension();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomExtension> getAllCustomExtensions() {
        return customExtensionRepository.findAllOrderByCreatedAt();
    }

    @Override
    public FixedExtension updateFixedExtensionStatus(String extension, boolean isBlocked) {
        FixedExtension fixedExtension = fixedExtensionRepository.findByExtension(extension.toLowerCase())
                .orElseThrow(() -> BusinessException.notFound("고정 확장자를 찾을 수 없습니다: " + extension));

        fixedExtension.updateBlockStatus(isBlocked);
        return fixedExtensionRepository.save(fixedExtension);
    }

    @Override
    public FixedExtension addFixedExtension(String extension, String description) {
        String normalizedExtension = extension.toLowerCase().trim();

        if (!validateExtension(normalizedExtension)) {
            throw BusinessException.badRequest("유효하지 않은 확장자입니다: " + extension);
        }

        if (fixedExtensionRepository.findAll().size() >= MAX_FIXED_EXTENSIONS) {
            throw BusinessException.badRequest("고정 확장자는 최대 " + MAX_FIXED_EXTENSIONS + "개까지 추가할 수 있습니다.");
        }

        if (fixedExtensionRepository.existsByExtension(normalizedExtension)) {
            throw BusinessException.conflict("이미 존재하는 고정 확장자입니다: " + extension);
        }

        if (customExtensionRepository.existsByExtension(normalizedExtension)) {
            throw BusinessException.conflict("이미 커스텀 확장자에 존재합니다: " + extension);
        }

        FixedExtension fixedExtension = FixedExtension.builder()
                .extension(normalizedExtension)
                .isBlocked(false)
                .description(description != null ? description : getExtensionDescription(normalizedExtension))
                .build();

        return fixedExtensionRepository.save(fixedExtension);
    }

    @Override
    public void deleteFixedExtension(Long id) {
        FixedExtension fixedExtension = fixedExtensionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("고정 확장자를 찾을 수 없습니다."));

        fixedExtensionRepository.delete(fixedExtension);
    }

    @Override
    public CustomExtension addCustomExtension(String extension) {
        String normalizedExtension = extension.toLowerCase().trim();

        if (!validateExtension(normalizedExtension)) {
            throw BusinessException.badRequest("유효하지 않은 확장자입니다: " + extension);
        }

        if (customExtensionRepository.findAll().size() >= MAX_CUSTOM_EXTENSIONS) {
            throw BusinessException.badRequest("커스텀 확장자는 최대 " + MAX_CUSTOM_EXTENSIONS + "개까지 추가할 수 있습니다.");
        }

        if (fixedExtensionRepository.existsByExtension(normalizedExtension)) {
            throw BusinessException.conflict("이미 고정 확장자에 존재합니다: " + extension);
        }

        if (customExtensionRepository.existsByExtension(normalizedExtension)) {
            throw BusinessException.conflict("이미 추가된 커스텀 확장자입니다: " + extension);
        }

        CustomExtension customExtension = CustomExtension.builder()
                .extension(normalizedExtension)
                .build();

        return customExtensionRepository.save(customExtension);
    }

    @Override
    public void deleteCustomExtension(Long id) {
        CustomExtension customExtension = customExtensionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("커스텀 확장자를 찾을 수 없습니다."));

        customExtensionRepository.delete(customExtension);
    }

    @Override
    public void deleteCustomExtensionByExtension(String extension) {
        String normalizedExtension = extension.toLowerCase().trim();
        CustomExtension customExtension = customExtensionRepository.findByExtension(normalizedExtension)
                .orElseThrow(() -> BusinessException.notFound("커스텀 확장자를 찾을 수 없습니다: " + extension));

        customExtensionRepository.delete(customExtension);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExtensionBlocked(String extension) {
        String normalizedExtension = extension.toLowerCase();

        boolean isFixedBlocked = fixedExtensionRepository.findByExtension(normalizedExtension)
                .map(FixedExtension::isBlocked)
                .orElse(false);

        boolean isCustomBlocked = customExtensionRepository.findByExtension(normalizedExtension)
                .map(CustomExtension::isBlocked)
                .orElse(false);

        return isFixedBlocked || isCustomBlocked;
    }

    @Override
    @Transactional(readOnly = true)
    public String getExtensionBlockType(String extension) {
        String normalizedExtension = extension.toLowerCase();

        // 고정 확장자에서 차단되는지 확인
        boolean isFixedBlocked = fixedExtensionRepository.findByExtension(normalizedExtension)
                .map(FixedExtension::isBlocked)
                .orElse(false);

        if (isFixedBlocked) {
            return "fixed";
        }

        // 커스텀 확장자에서 차단되는지 확인
        boolean isCustomBlocked = customExtensionRepository.findByExtension(normalizedExtension)
                .map(CustomExtension::isBlocked)
                .orElse(false);

        if (isCustomBlocked) {
            return "custom";
        }

        return "none";
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllBlockedExtensions() {
        List<String> fixedBlocked = fixedExtensionRepository.findBlockedExtensions();
        List<String> customBlocked = customExtensionRepository.findBlockedExtensions();

        return Stream.concat(fixedBlocked.stream(), customBlocked.stream())
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public void initializeFixedExtensions() {
        for (String extension : DEFAULT_FIXED_EXTENSIONS) {
            if (!fixedExtensionRepository.existsByExtension(extension)) {
                FixedExtension fixedExtension = FixedExtension.builder()
                        .extension(extension)
                        .isBlocked(false)
                        .description(getExtensionDescription(extension))
                        .build();

                fixedExtensionRepository.save(fixedExtension);
                log.info("고정 확장자 초기화: {}", extension);
            }
        }
    }

    @Override
    public boolean validateExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }

        String trimmed = extension.trim();
        if (trimmed.length() > MAX_EXTENSION_LENGTH) {
            return false;
        }

        return trimmed.matches("^[a-zA-Z0-9]+$");
    }


    @Override
    @Transactional
    public int deleteAllCustomExtensions() {
        List<CustomExtension> allCustomExtensions = customExtensionRepository.findAll();
        int count = allCustomExtensions.size();
        customExtensionRepository.deleteAll();
        log.info("모든 커스텀 확장자 삭제됨: {}개", count);
        return count;
    }

    @Override
    @Transactional
    public void resetFixedExtensions() {
        // 모든 고정 확장자 삭제
        fixedExtensionRepository.deleteAll();
        fixedExtensionRepository.flush(); // 강제로 DB에 반영
        log.info("모든 고정 확장자 삭제됨");

        // 기본 확장자 추가
        String[] defaultExtensions = {"bat", "cmd", "com", "cpl", "exe", "scr", "js"};
        for (String extension : defaultExtensions) {
            // 중복 체크 후 추가
            if (!fixedExtensionRepository.existsByExtension(extension)) {
                FixedExtension fixedExtension = FixedExtension.builder()
                        .extension(extension)
                        .description(getExtensionDescription(extension))
                        .isBlocked(false)
                        .build();
                fixedExtensionRepository.save(fixedExtension);
            }
        }
        log.info("기본 고정 확장자 {}개 초기화 완료", defaultExtensions.length);
    }

    private String getExtensionDescription(String extension) {
        return switch (extension) {
            case "bat" -> "배치 파일";
            case "cmd" -> "명령 파일";
            case "com" -> "실행 파일";
            case "cpl" -> "제어판 파일";
            case "exe" -> "실행 파일";
            case "scr" -> "화면보호기 파일";
            case "js" -> "자바스크립트 파일";
            default -> "실행 가능한 파일";
        };
    }
}
