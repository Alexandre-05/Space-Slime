package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.PlayerNameHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerNameHistoryRepository extends JpaRepository<PlayerNameHistoryEntity, Long> {
    List<PlayerNameHistoryEntity> findByUuidOrderByChangedAtDesc(String uuid);
}
