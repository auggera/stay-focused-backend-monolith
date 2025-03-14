package one.stayfocused.backend.service.avatar;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {
    String uploadAvatar(Long userId, MultipartFile file);
}
