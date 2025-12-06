import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class FSEntry implements Serializable{
    private static final long serialVersionUID = 1L;

    protected String name;
    protected  String path;
    protected long size;
    protected LocalDateTime creationTime;
    protected LocalDateTime lastModifiedTime;
    protected String permissions;
    protected int inode;

    private static int nextInode = 1;

    public FSEntry(String name, String path){
        this.name = name;
        this.path = path;
        this.creationTime = LocalDateTime.now();
        this.lastModifiedTime = LocalDateTime.now();
        this.inode = nextInode++;
        this.size = size = 0;

    }

    public abstract boolean isDirectory();
    public abstract String getType();

    public void updateModifiedTime(){
        this.lastModifiedTime = LocalDateTime.now();

    }

    public String getName() {return name;}
    public String getPath() {return path;}
    public long getSize() {return size;}
    public LocalDateTime getCreationTime() {return creationTime;}
    public LocalDateTime getLastModifiedTime() {return lastModifiedTime;}
    public String getPermissions() {return permissions;}
    public int getNode() {return inode;}

    public void setName(String name) {this.name = name;}
    public void setPath(String path) {this.path = path;}
    public void setSize(long size) {this.size = size;}
    public void setPermissions(String permissions) {this.permissions = permissions;}

    public String toListString(){
        return String.format("%s | %-10s | %-8d | %s | %s", 
            getType().charAt(0),
            this.permissions,
            this.size,
            this.lastModifiedTime.toLocalDate().toString(),
            this.name);
    }
}
