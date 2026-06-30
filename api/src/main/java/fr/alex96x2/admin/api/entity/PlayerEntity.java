package fr.alex96x2.admin.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "players")
public class PlayerEntity {

    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private String uuid;

    @Column(name = "current_name", nullable = false, length = 16)
    private String currentName;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    @Column(name = "total_playtime", nullable = false)
    private long totalPlaytime;

    @Column(name = "last_ip_hash", length = 64)
    private String lastIpHash;

    @Column(name = "last_ip_encrypted")
    private byte[] lastIpEncrypted;

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getCurrentName() { return currentName; }
    public void setCurrentName(String currentName) { this.currentName = currentName; }
    public Instant getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Instant firstSeen) { this.firstSeen = firstSeen; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
    public long getTotalPlaytime() { return totalPlaytime; }
    public void setTotalPlaytime(long totalPlaytime) { this.totalPlaytime = totalPlaytime; }
    public String getLastIpHash() { return lastIpHash; }
    public void setLastIpHash(String lastIpHash) { this.lastIpHash = lastIpHash; }
    public byte[] getLastIpEncrypted() { return lastIpEncrypted; }
    public void setLastIpEncrypted(byte[] lastIpEncrypted) { this.lastIpEncrypted = lastIpEncrypted; }

    public UUID uuidAsUuid() { return UUID.fromString(uuid); }
}
