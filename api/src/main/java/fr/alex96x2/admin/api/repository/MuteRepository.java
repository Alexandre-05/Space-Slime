package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.MuteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MuteRepository extends JpaRepository<MuteEntity, Long> {
    List<MuteEntity> findByUuidOrderByCreatedAtDesc(String uuid);
    Optional<MuteEntity> findFirstByUuidAndActiveTrueOrderByCreatedAtDesc(String uuid);
    long countByActiveTrue();
}
