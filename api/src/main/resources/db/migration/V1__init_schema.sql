CREATE TABLE players (
    uuid            CHAR(36)     NOT NULL PRIMARY KEY,
    current_name    VARCHAR(16)  NOT NULL,
    first_seen      DATETIME(3)  NOT NULL,
    last_seen       DATETIME(3)  NOT NULL,
    total_playtime  BIGINT       NOT NULL DEFAULT 0,
    last_ip_hash    VARCHAR(64)  NULL,
    last_ip_encrypted VARBINARY(64) NULL,
    INDEX idx_current_name (current_name),
    INDEX idx_last_seen (last_seen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE player_names_history (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid        CHAR(36)     NOT NULL,
    name        VARCHAR(16)  NOT NULL,
    changed_at  DATETIME(3)  NOT NULL,
    INDEX idx_pnh_uuid (uuid),
    CONSTRAINT fk_pnh_player FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sessions (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid        CHAR(36)     NOT NULL,
    join_at     DATETIME(3)  NOT NULL,
    quit_at     DATETIME(3)  NULL,
    ip_hash     VARCHAR(64)  NULL,
    ip_encrypted VARBINARY(64) NULL,
    INDEX idx_sessions_uuid (uuid),
    INDEX idx_sessions_join (join_at),
    CONSTRAINT fk_sessions_player FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bans (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid        CHAR(36)     NOT NULL,
    staff_uuid  CHAR(36)     NULL,
    staff_name  VARCHAR(16)  NULL,
    reason      TEXT         NOT NULL,
    created_at  DATETIME(3)  NOT NULL,
    expires_at  DATETIME(3)  NULL,
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    lifted_by   VARCHAR(64)  NULL,
    lifted_at   DATETIME(3)  NULL,
    source      ENUM('INGAME', 'WEB') NOT NULL DEFAULT 'INGAME',
    INDEX idx_bans_uuid (uuid),
    INDEX idx_bans_active (active),
    CONSTRAINT fk_bans_player FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mutes (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid        CHAR(36)     NOT NULL,
    staff_uuid  CHAR(36)     NULL,
    staff_name  VARCHAR(16)  NULL,
    reason      TEXT         NOT NULL,
    created_at  DATETIME(3)  NOT NULL,
    expires_at  DATETIME(3)  NULL,
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    lifted_by   VARCHAR(64)  NULL,
    lifted_at   DATETIME(3)  NULL,
    source      ENUM('INGAME', 'WEB') NOT NULL DEFAULT 'INGAME',
    INDEX idx_mutes_uuid (uuid),
    INDEX idx_mutes_active (active),
    CONSTRAINT fk_mutes_player FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE kicks (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid        CHAR(36)     NOT NULL,
    staff_uuid  CHAR(36)     NULL,
    staff_name  VARCHAR(16)  NULL,
    reason      TEXT         NOT NULL,
    created_at  DATETIME(3)  NOT NULL,
    source      ENUM('INGAME', 'WEB') NOT NULL DEFAULT 'INGAME',
    INDEX idx_kicks_uuid (uuid),
    CONSTRAINT fk_kicks_player FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE warns (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid        CHAR(36)     NOT NULL,
    staff_uuid  CHAR(36)     NULL,
    staff_name  VARCHAR(16)  NULL,
    reason      TEXT         NOT NULL,
    created_at  DATETIME(3)  NOT NULL,
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    lifted_by   VARCHAR(64)  NULL,
    lifted_at   DATETIME(3)  NULL,
    source      ENUM('INGAME', 'WEB') NOT NULL DEFAULT 'INGAME',
    INDEX idx_warns_uuid (uuid),
    CONSTRAINT fk_warns_player FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE staff_notes (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid        CHAR(36)     NOT NULL,
    staff_id    BIGINT       NULL,
    staff_name  VARCHAR(32)  NULL,
    content     TEXT         NOT NULL,
    created_at  DATETIME(3)  NOT NULL,
    INDEX idx_notes_uuid (uuid),
    CONSTRAINT fk_notes_player FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE staff_accounts (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(32)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            ENUM('MODERATEUR', 'ADMIN', 'FONDATEUR') NOT NULL DEFAULT 'MODERATEUR',
    created_at      DATETIME(3)  NOT NULL,
    last_login      DATETIME(3)  NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE staff_actions_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    staff_id    BIGINT       NULL,
    staff_name  VARCHAR(32)  NULL,
    action_type VARCHAR(64)  NOT NULL,
    target_uuid CHAR(36)     NULL,
    details     JSON         NULL,
    created_at  DATETIME(3)  NOT NULL,
    source      ENUM('INGAME', 'WEB') NOT NULL DEFAULT 'WEB',
    INDEX idx_log_created (created_at),
    INDEX idx_log_target (target_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pending_actions (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    action_type VARCHAR(32)  NOT NULL,
    target_uuid CHAR(36)     NOT NULL,
    payload     JSON         NOT NULL,
    created_at  DATETIME(3)  NOT NULL,
    processed   TINYINT(1)   NOT NULL DEFAULT 0,
    INDEX idx_pending_processed (processed, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
