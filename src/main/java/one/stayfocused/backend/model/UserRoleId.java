package one.stayfocused.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@Embeddable
public class UserRoleId implements Serializable {
    @Column(name = "user_id")
    private final Long userId;

    @Column(name = "role_id")
    private final Long roleId;

    protected UserRoleId() {
        this.userId = null;
        this.roleId = null;
    }
}

