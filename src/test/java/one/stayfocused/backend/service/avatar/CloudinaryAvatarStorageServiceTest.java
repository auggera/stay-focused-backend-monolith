package one.stayfocused.backend.service.avatar;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryAvatarStorageServiceTest {

    private static final long USER_ID = 1;
    private static final String SECURE_URL = "https://cloudinary.com/mock-image.jpg";

    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;
    @InjectMocks private CloudinaryAvatarStorageService avatarStorageService;

    @Test
    void testShouldUploadAvatarToCloudinary() throws IOException {
        MultipartFile file = new MockMultipartFile("avatar.jpg", new byte[10]);
        Map<String, String>  mockResponse = Map.of("secure_url", SECURE_URL);

        doReturn(uploader)
                .when(cloudinary).uploader();

        doReturn(mockResponse)
                .when(uploader).upload(eq(file.getBytes()), anyMap());

        String url = avatarStorageService.uploadAvatar(USER_ID, file);

        verify(cloudinary).uploader();
        verify(uploader).upload(eq(file.getBytes()), anyMap());
        assertEquals(SECURE_URL, url);
    }
}
