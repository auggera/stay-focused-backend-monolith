package one.stayfocused.backend.service.avatar;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AvatarStorageFactory {

    private final Map<StorageType, AvatarStorageService> serviceMap;

    public AvatarStorageFactory(List<AvatarStorageService> services) {
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(AvatarStorageService::getStorageType, Function.identity()));
    }

    public AvatarStorageService getService(StorageType type) {
        return serviceMap.get(type);
    }

}
