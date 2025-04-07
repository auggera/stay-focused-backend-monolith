package one.stayfocused.backend.storage;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StoragePathBuilder {

    public String buildPublicId(ResourceType resourceType, StorageType storageType, String ownerLabel, Long ownerId) {
        return String.format("%s/%s/%s_%d_%s",
                resourceType.name().toLowerCase(),
                storageType.name().toLowerCase(),
                ownerLabel,
                ownerId,
                UUID.randomUUID());
    }
}
