package fr.alex96x2.admin.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sessions")
public class SessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String uuid;
    @Column(name = "join_at", nullable = false)
    private Instant joinAt;
    @Column(name = "quit_at")
    private Instant quitAt;
    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public Instant getJoinAt() { return joinAt; }
    public Instant getQuitAt() { return quitAt; }
    public String getIpHash() { return ipHash; }
}
