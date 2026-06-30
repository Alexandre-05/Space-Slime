package fr.alex96x2.admin.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "staff_notes")
public class StaffNoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String uuid;
    @Column(name = "staff_name", length = 32)
    private String staffName;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getStaffName() { return staffName; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
