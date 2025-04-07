package one.stayfocused.backend.config;

import lombok.Getter;
import lombok.Setter;
import one.stayfocused.backend.storage.StorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageConfig {

    private ResourceStorage avatar;

    @Getter
    @Setter
    public static class ResourceStorage {
        private StorageType type;
    }
}
