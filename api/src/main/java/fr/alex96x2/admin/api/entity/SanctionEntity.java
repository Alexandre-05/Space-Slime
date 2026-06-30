package fr.alex96x2.admin.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

@MappedSuperclass
public abstract class SanctionEntity {

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

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "lifted_by", length = 64)
    private String liftedBy;

    @Column(name = "lifted_at")
    private Instant liftedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    public enum Source { INGAME, WEB }

    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getStaffUuid() { return staffUuid; }
    public void setStaffUuid(String staffUuid) { this.staffUuid = staffUuid; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getLiftedBy() { return liftedBy; }
    public void setLiftedBy(String liftedBy) { this.liftedBy = liftedBy; }
    public Instant getLiftedAt() { return liftedAt; }
    public void setLiftedAt(Instant liftedAt) { this.liftedAt = liftedAt; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
}
