package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    List<SessionEntity> findByUuidOrderByJoinAtDesc(String uuid);
}
