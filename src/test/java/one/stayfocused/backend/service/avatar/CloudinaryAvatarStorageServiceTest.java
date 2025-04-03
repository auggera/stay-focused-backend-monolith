package one.stayfocused.backend.service.avatar;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import one.stayfocused.backend.exception.AvatarDeletionException;
import one.stayfocused.backend.exception.AvatarUploadException;
import one.stayfocused.backend.storage.ResourceType;
import one.stayfocused.backend.storage.StoragePathBuilder;
import one.stayfocused.backend.storage.StoragePathResolver;
import one.stayfocused.backend.storage.StorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryAvatarStorageServiceTest {

    private static final long USER_ID = 1;
    private static final String MOCK_PUBLIC_ID = "avatars/cloudinary/user_1";
    private static final String MOCK_SECURE_URL = "https://cloudinary.com/avatars/cloudinary/user_1_abc123.png";

    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;
    @Mock private StoragePathBuilder storagePathBuilder;
    @Mock private StoragePathResolver storagePathResolver;
    @InjectMocks private CloudinaryAvatarStorageService avatarStorageService;

    @Test
    void testShouldUploadAvatarToCloudinary() throws IOException {
        MultipartFile file = new MockMultipartFile("avatar.jpg", new byte[10]);
        Map<String, String>  mockResponse = Map.of("secure_url", MOCK_SECURE_URL);

        doReturn(uploader)
                .when(cloudinary).uploader();

        doReturn(mockResponse)
                .when(uploader).upload(eq(file.getBytes()), anyMap());

        doReturn(MOCK_PUBLIC_ID)
                .when(storagePathBuilder).buildPublicId(ResourceType.AVATAR, StorageType.CLOUDINARY, "user", USER_ID);

        String url = avatarStorageService.uploadAvatar(USER_ID, file);

        verify(cloudinary).uploader();
        verify(storagePathBuilder).buildPublicId(ResourceType.AVATAR, StorageType.CLOUDINARY, "user", USER_ID);
        verify(uploader).upload(eq(file.getBytes()), anyMap());
        assertEquals(MOCK_SECURE_URL, url);
    }

    @Test
    void testShouldThrowAvatarUploadExceptionOnFailure() throws IOException {
        MultipartFile file = new MockMultipartFile("avatar.jpg", new byte[10]);

        doReturn(uploader)
                .when(cloudinary).uploader();

        doThrow(new IOException("Simulated failure"))
                .when(uploader).upload(eq(file.getBytes()), anyMap());

        AvatarUploadException exception = assertThrows(
                AvatarUploadException.class,
                () -> avatarStorageService.uploadAvatar(USER_ID, file)
        );

        assertEquals("Failed to upload avatar to Cloudinary", exception.getMessage());

        verify(cloudinary).uploader();
        verify(uploader).upload(eq(file.getBytes()), anyMap());
    }

    @Test
    void shouldDeleteAvatarSuccessfully() throws IOException {
        StoragePathResolver.ResolvedPathParts parts = new StoragePathResolver.ResolvedPathParts(
                "avatars", "cloudinary", "user", USER_ID
        );

        doReturn(Optional.of(parts))
                .when(storagePathResolver).resolveFull(MOCK_SECURE_URL);

        Map<String, String> mockResult = Map.of("result", "ok");

        doReturn(uploader)
                .when(cloudinary).uploader();

        doReturn(mockResult)
                .when(uploader).destroy(eq(MOCK_PUBLIC_ID), anyMap());

        avatarStorageService.deleteAvatar(MOCK_SECURE_URL);

        verify(storagePathResolver).resolveFull(MOCK_SECURE_URL);
        verify(cloudinary).uploader();
        verify(uploader).destroy(eq(MOCK_PUBLIC_ID), anyMap());
    }

    @Test
    void shouldLogWarningIfResultNotOk() throws IOException {
        final String resourceUrl = "https://unknown.com/...";

        StoragePathResolver.ResolvedPathParts parts = new StoragePathResolver.ResolvedPathParts("avatars", "cloudinary", "user", USER_ID);

        doReturn(Optional.of(parts))
                .when(storagePathResolver).resolveFull(resourceUrl);

        Map<String, String> mockResult = Map.of("result", "not_found");

        doReturn(uploader)
                .when(cloudinary).uploader();

        doReturn(mockResult)
                .when(uploader).destroy(eq(MOCK_PUBLIC_ID), anyMap());

        avatarStorageService.deleteAvatar(resourceUrl);

        verify(storagePathResolver).resolveFull(resourceUrl);
        verify(cloudinary).uploader();
        verify(uploader).destroy(eq(MOCK_PUBLIC_ID), anyMap());
    }

    @Test
    void shouldThrowExceptionOnInvalidPath() {
        final String invalidUrl = "invalid_url";

        doReturn(Optional.empty())
                .when(storagePathResolver).resolveFull(invalidUrl);

        assertThrows(AvatarDeletionException.class,
                () -> avatarStorageService.deleteAvatar(invalidUrl));
    }

    @Test
    void shouldThrowExceptionOnCloudinaryFailure() throws IOException {
        StoragePathResolver.ResolvedPathParts parts = new StoragePathResolver.ResolvedPathParts(
                "avatars", "cloudinary", "user", USER_ID
        );

        doReturn(Optional.of(parts))
                .when(storagePathResolver).resolveFull(MOCK_SECURE_URL);

        doReturn(uploader)
                .when(cloudinary).uploader();

        doThrow(new IOException("Simulated failure"))
                .when(uploader).destroy(eq(MOCK_PUBLIC_ID), anyMap());

        assertThrows(AvatarDeletionException.class,
                () -> avatarStorageService.deleteAvatar(MOCK_SECURE_URL));
    }
}
