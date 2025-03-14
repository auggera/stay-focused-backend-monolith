package one.stayfocused.backend.service.avatar;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.stayfocused.backend.exception.AvatarUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryAvatarStorageService implements AvatarStorageService {

    private static final String AVATARS_PATH = "avatars/";

    private final Cloudinary cloudinary;

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", AVATARS_PATH,
                    "public_id", buildPublicId(userId),
                    "overwrite", true,
                    "resource_type", "image"
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload avatar to Cloudinary. {}", e.getMessage(), e);
            throw new AvatarUploadException("Failed to upload avatar to Cloudinary");
        }
    }

    private String buildPublicId(Long userId) {
        return AVATARS_PATH + "user_" + userId + "_" + UUID.randomUUID();
    }
}