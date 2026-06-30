package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.PlayerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<PlayerEntity, String> {

    @Query("""
            SELECT p FROM PlayerEntity p
            WHERE (:search IS NULL OR :search = '' OR LOWER(p.currentName) LIKE LOWER(CONCAT('%', :search, '%')) OR p.uuid LIKE CONCAT('%', :search, '%'))
            """)
    Page<PlayerEntity> search(@Param("search") String search, Pageable pageable);
}
