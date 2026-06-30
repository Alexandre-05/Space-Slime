package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.KickEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KickRepository extends JpaRepository<KickEntity, Long> {
    List<KickEntity> findByUuidOrderByCreatedAtDesc(String uuid);
}
