package fr.alex96x2.admin.storage.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonDataFile {

    public long nextBanId = 1;
    public long nextMuteId = 1;
    public long nextKickId = 1;
    public long nextWarnId = 1;
    public long nextNoteId = 1;
    public long nextSessionId = 1;
    public long nextNameHistoryId = 1;

    public Map<String, JsonPlayer> players = new HashMap<>();
    public List<JsonNameHistory> nameHistory = new ArrayList<>();
    public List<JsonSession> sessions = new ArrayList<>();
    public List<JsonSanction> bans = new ArrayList<>();
    public List<JsonSanction> mutes = new ArrayList<>();
    public List<JsonKick> kicks = new ArrayList<>();
    public List<JsonWarn> warns = new ArrayList<>();
    public List<JsonNote> notes = new ArrayList<>();

    public static class JsonPlayer {
        public String uuid;
        public String currentName;
        public long firstSeen;
        public long lastSeen;
        public long totalPlaytime;
        public String lastIpHash;
        public String lastIpEncrypted;
    }

    public static class JsonNameHistory {
        public long id;
        public String uuid;
        public String name;
        public long changedAt;
    }

    public static class JsonSession {
        public long id;
        public String uuid;
        public long joinAt;
        public Long quitAt;
        public String ipHash;
        public String ipEncrypted;
    }

    public static class JsonSanction {
        public long id;
        public String uuid;
        public String staffUuid;
        public String staffName;
        public String reason;
        public long createdAt;
        public Long expiresAt;
        public boolean active;
        public String liftedBy;
        public Long liftedAt;
        public String source;
    }

    public static class JsonKick {
        public long id;
        public String uuid;
        public String staffUuid;
        public String staffName;
        public String reason;
        public long createdAt;
        public String source;
    }

    public static class JsonWarn {
        public long id;
        public String uuid;
        public String staffUuid;
        public String staffName;
        public String reason;
        public long createdAt;
        public boolean active;
        public String source;
    }

    public static class JsonNote {
        public long id;
        public String uuid;
        public String staffName;
        public String content;
        public long createdAt;
    }
}
