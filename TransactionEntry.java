import java.io.Serializable;
import java.time.LocalDateTime;

public class TransactionEntry implements Serializable{
    private static final long serialVersionUID = 1L;

    public enum OperationType{
        CREATE_FILE, DELETE_FILE, RENAME_FILE,
        CREATE_DIR, DELETE_DIR, RENAME_DIR,
        UPDATE_CONTENT, COMMIT
    }
    
    private OperationType type;
    private LocalDateTime timestamp;
    private String affectedPath;
    private String payload;

    public TransactionEntry(OperationType type, String affectedPath, String payload){
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.affectedPath = affectedPath;
        this.payload = payload;

    }

    public OperationType getType() {return type;}
    public LocalDateTime getTimestamp() {return timestamp;}
    public String getAffectedPath() {return affectedPath;}
    public String getPayload() {return payload;}

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (Payload: %s)", 
            timestamp.toString(), type.name(), affectedPath, 
            payload != null ? payload.substring(0, Math.min(20, payload.length())) + "..." : "N/A");
    }
}
