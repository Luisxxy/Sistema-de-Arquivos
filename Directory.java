import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Directory extends FSEntry implements Serializable{
    private static final long  serialVersionUID = 1L;
    
    private List<FSEntry> children;

    public Directory(String name, String path){
        super(name, path);
        this.children = new ArrayList<>();
        this.permissions = "rwxr-xr-x";
        
    }

    @Override
    public boolean isDirectory(){
        return true;

    }

    @Override
    public String getType(){
        return "DIR";

    }

    public void addChild(FSEntry entry){
        this.children.add(entry);
        updateModifiedTime();
        this.size = this.children.size();

    }

    public boolean removeChild(String name) {
        boolean removed = this.children.removeIf(entry -> entry.getName().equals(name));
        if (removed) {
            updateModifiedTime();
            this.size = this.children.size();
        }
        return removed;
    }

    public FSEntry getChild(String name){
        for(FSEntry entry : children){
            if (entry.getName().equals(name)){
                return entry;

            }
        }
        return null;

    }

    public List<FSEntry> getChildren(){
        return children;

    }

    @Override
    public String toListString(){
        return String.format("d%s | %-10s | %-8s | %s | %s",
        this.permissions.substring(1),
        this.permissions,
        "",
        this.lastModifiedTime.toLocalDate().toString(),
        this.name);

    }
}
