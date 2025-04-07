package one.stayfocused.backend.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class StoragePathResolver {

    private static final Pattern PATH_PATTERN = Pattern.compile(
            "(?:.*/)?(?<resource>[^/]+)/(?<storage>[^/]+)/(?<owner>[^_]+)_(?<id>\\d+)_(?<uuid>[a-zA-Z0-9\\-]+)"
    );

    public Optional<StorageType> resolveStorageType(String urlOrPublicId) {
        return extract(urlOrPublicId)
                .flatMap(
                        parts -> StorageType.fromAlias(parts.storageRaw
                        ));
    }

    public Optional<ResourceType> resolveResourceType(String urlOrPublicId) {
        return extract(urlOrPublicId)
                .flatMap(
                        parts -> ResourceType.fromAlias(parts.resourceRaw
                        ));
    }

    public Optional<ResolvedPathParts> resolveFull(String urlOrPublicId) {
        return extract(urlOrPublicId);
    }

    private Optional<ResolvedPathParts> extract(String input) {
        String path = normalizeToPath(input);
        Matcher matcher = PATH_PATTERN.matcher(path);

        if (matcher.find()) {
            String resource = matcher.group("resource");
            String storage = matcher.group("storage");
            String owner = matcher.group("owner");
            Long id = Long.valueOf(matcher.group("id"));
            String uuid = matcher.group("uuid");
            return Optional.of(new ResolvedPathParts(resource, storage, owner, id, uuid));
        }
        return Optional.empty();
    }

    private static String normalizeToPath(String input) {
        try {
            URI uri = new URI(input);
            return uri.getPath();
        } catch (URISyntaxException e) {
            return input;
        }
    }

    public record ResolvedPathParts(String resourceRaw,
                                     String storageRaw,
                                     String ownerLabel,
                                     Long ownerId,
                                     String uuid) {
    }
}
