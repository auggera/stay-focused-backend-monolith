package one.stayfocused.backend.service.avatar;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.stayfocused.backend.exception.AvatarUploadException;
import one.stayfocused.backend.storage.ResourceType;
import one.stayfocused.backend.storage.StoragePathBuilder;
import one.stayfocused.backend.storage.StorageType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryAvatarStorageService implements AvatarStorageService {

    private final StoragePathBuilder pathBuilder;
    private final Cloudinary cloudinary;

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", pathBuilder.buildPublicId(ResourceType.AVATAR, getStorageType(), "user", userId),
                    "overwrite", true,
                    "resource_type", "image"
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload avatar to Cloudinary. {}", e.getMessage(), e);
            throw new AvatarUploadException("Failed to upload avatar to Cloudinary");
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.CLOUDINARY;
    }
}