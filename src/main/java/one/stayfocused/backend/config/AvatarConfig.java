package one.stayfocused.backend.config;

import lombok.Getter;
import one.stayfocused.backend.storage.StorageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "avatars")
public class AvatarConfig {
    private List<String> defaultAvatarUrls;

    @Value("${avatars.storage-type}")
    private StorageType defaultStorageType;
}
