package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.WarnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WarnRepository extends JpaRepository<WarnEntity, Long> {
    List<WarnEntity> findByUuidAndActiveTrueOrderByCreatedAtDesc(String uuid);
    List<WarnEntity> findByUuidOrderByCreatedAtDesc(String uuid);
}
