import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable; 
import java.util.ArrayList;
import java.util.List;

public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String journalFilePath;
    private List<TransactionEntry> log;

    public Journal(String journalFilePath) {
        this.journalFilePath = journalFilePath;
        this.log = new ArrayList<>();
        loadLog();
    }
    
    public void addEntry(TransactionEntry entry) {
        this.log.add(entry);
        System.out.println("JOURNAL: Transação registrada: " + entry.getType() + " em " + entry.getAffectedPath());
    }
    
    public void saveLog() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(journalFilePath))) {
            oos.writeObject(log);
           
        } catch (IOException e) {
            System.err.println("ERRO JOURNAL: Falha ao salvar o log: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadLog() {
        File file = new File(journalFilePath);
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(journalFilePath))) {
                this.log = (List<TransactionEntry>) ois.readObject();
                System.out.println("JOURNAL: Log carregado com " + this.log.size() + " entradas.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("ERRO JOURNAL: Falha ao carregar o log. Iniciando vazio. " + e.getMessage());
                this.log = new ArrayList<>();
            }
        } else {
            System.out.println("JOURNAL: Arquivo de log não encontrado ou vazio. Iniciando novo log.");
        }
    }

    public void clearLog() {
        this.log.clear();
        saveLog(); 
        System.out.println("JOURNAL: Log limpo e checkpoint realizado.");
    }
    
    public List<TransactionEntry> getRecoveryLog() {
        List<TransactionEntry> recoveryList = new ArrayList<>();
        boolean inTransaction = false;
        
        for (TransactionEntry entry : this.log) {
            if (entry.getType() != TransactionEntry.OperationType.COMMIT && !inTransaction) {
                
                inTransaction = true;
                recoveryList.add(entry);
            } else if (entry.getType() != TransactionEntry.OperationType.COMMIT && inTransaction) {
                
                recoveryList.add(entry);
            } else if (entry.getType() == TransactionEntry.OperationType.COMMIT) {
                
                recoveryList.clear();
                inTransaction = false;
            }
        }
        
        if (!recoveryList.isEmpty()) {
            System.out.println("JOURNAL RECOVERY: Encontradas " + recoveryList.size() + " entradas de log pendentes (falha de sistema).");
         
        } else {
            System.out.println("JOURNAL RECOVERY: Nenhuma transação pendente encontrada. Sistema limpo.");
        }
        
        return recoveryList;
    }
}