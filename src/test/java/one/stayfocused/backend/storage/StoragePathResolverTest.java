package one.stayfocused.backend.storage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StoragePathResolverTest {

    private final StoragePathResolver resolver = new StoragePathResolver();

    private static final String MOCK_RESOURCE_URL = "https://res.cloudinary.com/demo/image/upload/v1/avatars/cloudinary/user_42_abc123.png";
    private static final String MOCK_RESOURCE_PUBLIC_ID = "avatars/cloudinary/user_42_abc123";

    @ParameterizedTest
    @MethodSource("resourcePathProvider")
    void shouldResolveStorageType_fromResourceUrl(String urlOrPublicId) {
        Optional<StorageType> storageType = resolver.resolveStorageType(urlOrPublicId);
        assertTrue(storageType.isPresent());
        assertEquals(StorageType.CLOUDINARY, storageType.get());
    }

    @ParameterizedTest
    @MethodSource("resourcePathProvider")
    void shouldResolveResourceType_fromResourceUrl(String urlOrPublicId) {
        Optional<ResourceType> resourceType = resolver.resolveResourceType(urlOrPublicId);

        assertTrue(resourceType.isPresent());
        assertEquals(ResourceType.AVATAR, resourceType.get());
    }

    @ParameterizedTest
    @MethodSource("resourcePathProvider")
    void shouldResolveAllParts_fromResourceUrl(String urlOrPublicId) {
        Optional<StoragePathResolver.ResolvedPathParts> parts = resolver.resolveFull(urlOrPublicId);

        assertTrue(parts.isPresent());
        assertEquals("avatars", parts.get().resourceRaw());
        assertEquals("cloudinary", parts.get().storageRaw());
        assertEquals("user", parts.get().ownerLabel());
        assertEquals(42L, (parts.get().ownerId()));
    }

    private static Stream<Arguments> resourcePathProvider() {
        return Stream.of(
                Arguments.of(MOCK_RESOURCE_URL),
                Arguments.of(MOCK_RESOURCE_PUBLIC_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidResourcePathProvider")
    void shouldReturnEmptyOptional_whenInvalidInput(String urlOrPublicId) {
        Optional<ResourceType> resourceType = resolver.resolveResourceType(urlOrPublicId);
        Optional<StorageType> storageType = resolver.resolveStorageType(urlOrPublicId);
        Optional<StoragePathResolver.ResolvedPathParts> parts = resolver.resolveFull(urlOrPublicId);

        assertTrue(resourceType.isEmpty(),  "Expected empty ResourceType for input: " + urlOrPublicId);
        assertTrue(storageType.isEmpty(),  "Expected empty StorageType for input: " + urlOrPublicId);
        assertTrue(parts.isEmpty(), "Expected empty ResolvedPathParts for input: " + urlOrPublicId);
    }

    private static Stream<Arguments> invalidResourcePathProvider() {
        return Stream.of(
                Arguments.of("invalidPath"),
                Arguments.of("path/not/exists"),
                Arguments.of("user_42_abc123"),
                Arguments.of("avatars/cloudinary/user_xyz_abc"),
                Arguments.of("https://example.com/image.jpg")
        );
    }
}
