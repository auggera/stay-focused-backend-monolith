package one.stayfocused.backend.exception;

import lombok.Getter;
import one.stayfocused.backend.model.RoleType;
import org.springframework.http.HttpStatus;

@Getter
public class RoleNotFoundException extends ApplicationException {
    private final RoleType roleType;

    public RoleNotFoundException(RoleType roleType) {
        super("Role not found: " + roleType, HttpStatus.NOT_FOUND);
        this.roleType = roleType;
    }
}
