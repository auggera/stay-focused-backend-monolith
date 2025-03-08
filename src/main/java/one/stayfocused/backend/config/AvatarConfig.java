package one.stayfocused.backend.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "avatars")
public class AvatarConfig {
    private List<String> defaultAvatarUrls;
}
