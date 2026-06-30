package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.BanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BanRepository extends JpaRepository<BanEntity, Long> {
    List<BanEntity> findByUuidOrderByCreatedAtDesc(String uuid);
    Optional<BanEntity> findFirstByUuidAndActiveTrueOrderByCreatedAtDesc(String uuid);
    long countByActiveTrue();
    List<BanEntity> findByActiveTrueOrderByCreatedAtDesc();
}
