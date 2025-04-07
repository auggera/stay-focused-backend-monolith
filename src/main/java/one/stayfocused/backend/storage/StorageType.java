package one.stayfocused.backend.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum StorageType {
    LOCAL("local"),
    CLOUDINARY("cloudinary");

    private final String alias;

    public static Optional<StorageType> fromAlias(String value) {
        return Arrays.stream(values())
                .filter(type -> type.alias.equals(value))
                .findFirst();
    }
}
