package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.StaffNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffNoteRepository extends JpaRepository<StaffNoteEntity, Long> {
    List<StaffNoteEntity> findByUuidOrderByCreatedAtDesc(String uuid);
}
