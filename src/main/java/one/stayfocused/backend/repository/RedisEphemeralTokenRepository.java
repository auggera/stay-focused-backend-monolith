package one.stayfocused.backend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class RedisEphemeralTokenRepository implements EphemeralTokenRepository {

    private static final String TOKEN_PREFIX = "ephemeral-token:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveToken(String tokenType, String identifier, String token, Duration expiration) {
        redisTemplate.opsForValue().set(buildTokenKey(tokenType, identifier), token, expiration);
    }

    @Override
    public String getToken(String tokenType, String identifier) {
        return redisTemplate.opsForValue().get(buildTokenKey(tokenType, identifier));
    }

    @Override
    public void deleteToken(String tokenType, String identifier) {
        redisTemplate.delete(buildTokenKey(tokenType, identifier));
    }
    private String buildTokenKey(String tokenType, String identifier) {
        return TOKEN_PREFIX + tokenType + ":" + identifier;
    }

}
