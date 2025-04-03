package one.stayfocused.backend.service.avatar;

import one.stayfocused.backend.storage.StorageType;
import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {
    String uploadAvatar(Long userId, MultipartFile file);
    StorageType getStorageType();
}
