package one.stayfocused.backend.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Getter
@Component
@ConfigurationProperties(prefix = "avatars")
public class AvatarConfig {

    private static final Random RANDOM = new Random();
    private List<String> defaultAvatarUrls;

    public boolean isDefaultAvatarUrl(String avatarUrl) {
        return defaultAvatarUrls.contains(avatarUrl);
    }

    public String assignRandomAvatar() {
        return defaultAvatarUrls.get(RANDOM.nextInt(defaultAvatarUrls.size()));
    }
}
