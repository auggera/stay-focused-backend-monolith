package one.stayfocused.backend.service.avatar;

import one.stayfocused.backend.exception.AvatarDeletionException;
import one.stayfocused.backend.exception.AvatarUploadException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CloudinaryAvatarStorageServiceIntegrationTest {

    @Autowired
    private CloudinaryAvatarStorageService cloudinaryAvatarStorageService;

    @Test
    void shouldUploadAndDeleteAvatarSuccessfully() throws AvatarUploadException, IOException {
        long userId = 1L;

        File file = new File("src/test/resources/test-avatar.jpg");
        MultipartFile multipartFile = new MockMultipartFile(
                "avatar.jpg",
                file.getName(),
                "image/jpeg",
                Files.readAllBytes(file.toPath())
        );

        String uploadedAvatarUrl = cloudinaryAvatarStorageService.uploadAvatar(userId, multipartFile);
        assertNotNull(uploadedAvatarUrl);
        System.out.println("Uploaded: " + uploadedAvatarUrl);

        assertDoesNotThrow(() -> cloudinaryAvatarStorageService.deleteAvatar(uploadedAvatarUrl));
    }

    @Test
    void shouldUploadAvatarSuccessfully_andFailWhenDeletingAvatar() throws AvatarUploadException {
        String invalidResourceUrl = "invalid-resource-url";

        assertThrows(AvatarDeletionException.class,
                () -> cloudinaryAvatarStorageService.deleteAvatar(invalidResourceUrl));
    }
}
