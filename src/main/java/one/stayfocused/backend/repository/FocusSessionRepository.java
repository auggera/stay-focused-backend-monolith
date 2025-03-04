package one.stayfocused.backend.repository;

import one.stayfocused.backend.model.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
}
