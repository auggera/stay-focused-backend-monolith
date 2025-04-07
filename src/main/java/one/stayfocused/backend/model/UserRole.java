package one.stayfocused.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "user_roles")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    private Role role;

    public UserRole(User user, Role role) {
        this.id = new UserRoleId(user.getId(), role.getId());
        this.user = user;
        this.role = role;
    }
}
