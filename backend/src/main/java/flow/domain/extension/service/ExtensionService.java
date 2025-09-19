package flow.domain.extension.service;

import flow.domain.extension.entity.CustomExtension;
import flow.domain.extension.entity.FixedExtension;

import java.util.List;

public interface ExtensionService {

    List<FixedExtension> getAllFixedExtensions();

    List<CustomExtension> getAllCustomExtensions();

    FixedExtension updateFixedExtensionStatus(String extension, boolean isBlocked);

    FixedExtension addFixedExtension(String extension, String description);

    void deleteFixedExtension(Long id);

    CustomExtension addCustomExtension(String extension);

    void deleteCustomExtension(Long id);

    void deleteCustomExtensionByExtension(String extension);

    boolean isExtensionBlocked(String extension);

    String getExtensionBlockType(String extension);

    List<String> getAllBlockedExtensions();

    void initializeFixedExtensions();

    boolean validateExtension(String extension);

    int deleteAllCustomExtensions();

    void resetFixedExtensions();

}
