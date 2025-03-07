package one.stayfocused.backend.repository;

import one.stayfocused.backend.model.Role;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByUserAndRole(User user, Role role);
}
