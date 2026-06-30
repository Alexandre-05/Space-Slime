package fr.alex96x2.admin.api.repository;

import fr.alex96x2.admin.api.entity.StaffAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StaffAccountRepository extends JpaRepository<StaffAccountEntity, Long> {
    Optional<StaffAccountEntity> findByUsername(String username);
}
