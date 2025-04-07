package one.stayfocused.backend.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum ResourceType {
    AVATAR("avatars");

    private final String alias;

    public static Optional<ResourceType> fromAlias(String value) {
        return Arrays.stream(values())
                .filter(type -> type.alias.equalsIgnoreCase(value))
                .findFirst();
    }
}
