package one.stayfocused.backend.service.avatar;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.stayfocused.backend.exception.AvatarDeletionException;
import one.stayfocused.backend.exception.AvatarUploadException;
import one.stayfocused.backend.storage.ResourceType;
import one.stayfocused.backend.storage.StoragePathBuilder;
import one.stayfocused.backend.storage.StoragePathResolver;
import one.stayfocused.backend.storage.StorageType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryAvatarStorageService implements AvatarStorageService {

    private final StoragePathBuilder storagePathBuilder;
    private final Cloudinary cloudinary;
    private final StoragePathResolver storagePathResolver;

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", storagePathBuilder.buildPublicId(ResourceType.AVATAR, getStorageType(), "user", userId),
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
    public void deleteAvatar(String avatarUrl) {
        String publicId = extractPublicId(avatarUrl);

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "image"
            ));

            Object status = result.get("result");

            if (!Objects.equals("ok", status)) {
                log.warn("Cloudinary returned non-ok status while deleting avatar: {}", status);
            }
        } catch (IOException e) {
            log.error("Failed to delete avatar from Cloudinary. {}", e.getMessage(), e);
            throw new AvatarDeletionException("Failed to delete avatar from Cloudinary");
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.CLOUDINARY;
    }

    private String extractPublicId(String resourceUrl) {
        return storagePathResolver.resolveFull(resourceUrl)
                .map(parts -> String.format("%s/%s/%s_%d",
                        parts.resourceRaw(),
                        parts.storageRaw(),
                        parts.ownerLabel(),
                        parts.ownerId()))
                .orElseThrow(() -> new AvatarDeletionException("Invalid avatar URL: cannot extract public_id"));
        }
}