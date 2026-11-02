import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditLog implements Serializable {
    private static List<AuditEntry> entries = new ArrayList<>();

    public static class AuditEntry implements Serializable {
        private String timestamp;
        private String actor;
        private String action;

        public AuditEntry(String actor, String action) {
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
            this.actor = (actor != null && !actor.isEmpty()) ? actor : "System";
            this.action = (action != null) ? action : "";
        }

        public String getTimestamp() { return timestamp; }
        public String getActor() { return actor; }
        public String getAction() { return action; }
    }

    public static synchronized void log(String actor, String action) {
        entries.add(0, new AuditEntry(actor, action));
    }

    public static List<AuditEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}