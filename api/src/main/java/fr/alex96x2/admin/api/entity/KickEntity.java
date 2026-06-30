package fr.alex96x2.admin.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "kicks")
public class KickEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String uuid;
    @Column(name = "staff_uuid", length = 36, columnDefinition = "CHAR(36)")
    private String staffUuid;
    @Column(name = "staff_name", length = 16)
    private String staffName;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SanctionEntity.Source source;

    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getStaffName() { return staffName; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
    public SanctionEntity.Source getSource() { return source; }
}
