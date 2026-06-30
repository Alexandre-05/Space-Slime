package fr.alex96x2.admin.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "player_names_history")
public class PlayerNameHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String uuid;
    @Column(nullable = false, length = 16)
    private String name;
    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public Instant getChangedAt() { return changedAt; }
}
