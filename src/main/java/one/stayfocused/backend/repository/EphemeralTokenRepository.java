package one.stayfocused.backend.repository;

import java.time.Duration;

public interface EphemeralTokenRepository {

    void saveToken(String tokenType, String identifier, String token, Duration expiration);
    String getToken(String tokenType, String identifier);
    void deleteToken(String tokenType, String identifier);
}
