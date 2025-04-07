package one.stayfocused.backend.repository;

import one.stayfocused.backend.model.Role;
import one.stayfocused.backend.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);
}
