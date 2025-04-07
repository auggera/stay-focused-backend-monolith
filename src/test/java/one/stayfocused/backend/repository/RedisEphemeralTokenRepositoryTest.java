package one.stayfocused.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RedisEphemeralTokenRepositoryTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private static final String TOKEN_PREFIX = "ephemeral-token:";
    private static final String TOKEN_TYPE = "token-type:";
    private static final String IDENTIFIER = "identifier:";
    private static final String TOKEN = "mocked-token:";
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(5);


    private  EphemeralTokenRepository tokenRepository;


    @BeforeEach
    void setUp() {
        tokenRepository = new RedisEphemeralTokenRepository(redisTemplate);
    }

    @Test
    void testShouldSaveToken() {
        final String tokenKey = buildTokenKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doNothing()
                .when(valueOperations).set(tokenKey, TOKEN,  TOKEN_EXPIRATION);

        tokenRepository.saveToken(TOKEN_TYPE, IDENTIFIER, TOKEN, TOKEN_EXPIRATION);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(tokenKey, TOKEN,  TOKEN_EXPIRATION);
    }

    @Test
    void testShouldReturnToken() {
        final String tokenKey = buildTokenKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(TOKEN)
                .when(valueOperations).get(tokenKey);

        String storedToken = tokenRepository.getToken(TOKEN_TYPE, IDENTIFIER);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(tokenKey);

        assertEquals(TOKEN, storedToken);
    }

    @Test
    void testShouldReturnNull_whenTokenDoesNotExists() {
        final String tokenKey = buildTokenKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(null)
                .when(valueOperations).get(TOKEN);

        String storedToken = tokenRepository.getToken(TOKEN_TYPE, IDENTIFIER);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(tokenKey);
        assertNull(storedToken);
    }

    @Test
    void testShouldDeleteToken() {
        final String tokenKey = buildTokenKey();

        doReturn(true)
                .when(redisTemplate).delete(tokenKey);

        tokenRepository.deleteToken(TOKEN_TYPE, IDENTIFIER);

        verify(redisTemplate).delete(tokenKey);
    }

    private String buildTokenKey() {
        return TOKEN_PREFIX + TOKEN_TYPE + ":" + IDENTIFIER;
    }
}
