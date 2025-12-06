import java.io.Serializable;
import java.util.UUID;

public class File extends FSEntry implements Serializable{
    private static final long serialVersionUID = 1L;

    private String simulatedBlockId;

    public File(String name, String path){
        super(name, path);
        this.permissions = "rw-r--r--";
        this.simulatedBlockId = UUID.randomUUID().toString();

    }

    @Override
    public boolean isDirectory(){
        return false;

    }

    @Override
    public String getType(){
        return "FILE";

    }

    public void writeContent(String content){
        this.size = content.length();
        System.out.println("DEBUG: Conteudo de  " + this.name + "simulado como escrito. Novo tamanho: " + this.size);

    }

    public String getSimulatedBlockId(){
        return simulatedBlockId;
        
    }
}
