import java.util.*;

public class FileSystemSimulator {
    private final Node root;
    private final List<TransactionEntry> transactions;

    private static class Node {
        String name;
        boolean directory;
        String content; // only for files
        Map<String, Node> children; // only for directories
        Node parent;

        Node(String name, boolean directory, Node parent) {
            this.name = name;
            this.directory = directory;
            this.parent = parent;
            if (directory) children = new HashMap<>();
            else content = "";
        }

        boolean isDirectory() { return directory; }
        boolean isFile() { return !directory; }
    }

    public FileSystemSimulator() {
        this.root = new Node("", true, null);
        this.transactions = new ArrayList<>();
    }

    private String normalize(String path) {
        if (path == null || path.isBlank()) return "";
        path = path.replace('\\', '/').trim();
        if (path.startsWith("/")) path = path.substring(1);
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }

    private Node traverseTo(String path) {
        String p = normalize(path);
        if (p.isEmpty()) return root;
        String[] parts = p.split("/");
        Node cur = root;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!cur.isDirectory()) return null;
            cur = cur.children.get(part);
            if (cur == null) return null;
        }
        return cur;
    }

    private Node ensureParent(String path, boolean createMissingDirs) {
        String p = normalize(path);
        if (p.isEmpty()) return root;
        int idx = p.lastIndexOf('/');
        String parentPath = idx >= 0 ? p.substring(0, idx) : "";
        Node parent = traverseTo(parentPath);
        if (parent == null && createMissingDirs) {
            // create missing path directories
            createDirectory(parentPath);
            parent = traverseTo(parentPath);
        }
        return parent;
    }

    private String basename(String path) {
        String p = normalize(path);
        if (p.isEmpty()) return "";
        int idx = p.lastIndexOf('/');
        return idx >= 0 ? p.substring(idx + 1) : p;
    }

    // Files

    public void createFile(String path) { createFile(path, ""); }

    public void createFile(String path, String content) {
        String p = normalize(path);
        if (p.isEmpty()) {
            System.out.println("Nome inválido.");
            return;
        }
        Node parent = ensureParent(p, true);
        if (parent == null || parent.isFile()) {
            System.out.println("Diretório pai inválido.");
            return;
        }
        String name = basename(p);
        if (parent.children.containsKey(name)) {
            System.out.println("Já existe um item com esse nome.");
            return;
        }
        Node file = new Node(name, false, parent);
        file.content = content == null ? "" : content;
        parent.children.put(name, file);
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.CREATE_FILE, p, "len=" + file.content.length()));
        System.out.println("Arquivo criado: " + p);
    }

    public void deleteFile(String path) {
        Node node = traverseTo(path);
        if (node == null || node.isDirectory()) {
            System.out.println("Arquivo não encontrado.");
            return;
        }
        node.parent.children.remove(node.name);
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.DELETE_FILE, normalize(path), "deletado"));
        System.out.println("Arquivo deletado: " + normalize(path));
    }

    public void copyFile(String srcPath, String dstPath) {
        Node src = traverseTo(srcPath);
        if (src == null || src.isDirectory()) {
            System.out.println("Arquivo de origem inválido.");
            return;
        }
        String dstNorm = normalize(dstPath);
        Node dstNode = traverseTo(dstNorm);
        if (dstNode != null && dstNode.isDirectory()) {
            // copy into directory keeping same basename
            String newPath = (dstNorm.isEmpty() ? "" : dstNorm + "/") + src.name;
            createFile(newPath, src.content);
            transactions.add(new TransactionEntry(TransactionEntry.OperationType.CREATE_FILE, newPath, "copiado de " + normalize(srcPath)));
            System.out.println("Arquivo copiado para: " + newPath);
            return;
        }
        Node dstParent = ensureParent(dstNorm, true);
        if (dstParent == null || dstParent.isFile()) {
            System.out.println("Destino inválido.");
            return;
        }
        String name = basename(dstNorm);
        if (name.isEmpty()) name = src.name;
        Node copy = new Node(name, false, dstParent);
        copy.content = src.content;
        dstParent.children.put(name, copy);
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.CREATE_FILE, (dstParent == root ? name : normalize(dstParent == null ? name : (normalize(getFullPath(dstParent)) + "/" + name))), "copiado de " + normalize(srcPath)));
        System.out.println("Arquivo copiado para: " + (dstParent == root ? name : normalize(getFullPath(dstParent) + "/" + name)));
    }

    public void renameFile(String srcPath, String dstPath) {
        Node src = traverseTo(srcPath);
        if (src == null || src.isDirectory()) {
            System.out.println("Arquivo de origem inválido.");
            return;
        }
        String dstNorm = normalize(dstPath);
        Node dstParent = ensureParent(dstNorm, true);
        if (dstParent == null || dstParent.isFile()) {
            System.out.println("Destino inválido.");
            return;
        }
        String newName = basename(dstNorm);
        if (newName.isEmpty()) newName = src.name;
        // remove from old parent and add to new parent
        src.parent.children.remove(src.name);
        src.name = newName;
        src.parent = dstParent;
        dstParent.children.put(newName, src);
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.RENAME_FILE, normalize(dstPath), "renomeado de " + normalize(srcPath)));
        System.out.println("Arquivo renomeado/movido para: " + normalize(getFullPath(src)));
    }

    // Directories

    public void createDirectory(String path) {
        String p = normalize(path);
        if (p.isEmpty()) {
            System.out.println("Criando raiz (já existe).");
            return;
        }
        String[] parts = p.split("/");
        Node cur = root;
        String built = "";
        for (String part : parts) {
            if (part.isEmpty()) continue;
            built = built.isEmpty() ? part : built + "/" + part;
            Node next = cur.children.get(part);
            if (next == null) {
                next = new Node(part, true, cur);
                cur.children.put(part, next);
                transactions.add(new TransactionEntry(TransactionEntry.OperationType.CREATE_DIR, built, "criado"));
            } else if (next.isFile()) {
                System.out.println("Já existe um arquivo com o nome de diretório: " + built);
                return;
            }
            cur = next;
        }
        System.out.println("Diretório criado: " + p);
    }

    public void deleteDirectory(String path) {
        Node dir = traverseTo(path);
        if (dir == null || dir.isFile()) {
            System.out.println("Diretório não encontrado.");
            return;
        }
        if (dir == root) {
            System.out.println("Não é possível deletar a raiz.");
            return;
        }
        // recursive delete
        String full = normalize(getFullPath(dir));
        dir.parent.children.remove(dir.name);
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.DELETE_DIR, full, "deletado (recursivo)"));
        System.out.println("Diretório deletado: " + full);
    }

    public void renameDirectory(String srcPath, String dstPath) {
        Node src = traverseTo(srcPath);
        if (src == null || src.isFile()) {
            System.out.println("Diretório de origem inválido.");
            return;
        }
        if (src == root) {
            System.out.println("Não é possível renomear a raiz.");
            return;
        }
        String dstNorm = normalize(dstPath);
        Node dstParent = ensureParent(dstNorm, true);
        if (dstParent == null || dstParent.isFile()) {
            System.out.println("Destino inválido.");
            return;
        }
        String newName = basename(dstNorm);
        if (newName.isEmpty()) newName = src.name;
        src.parent.children.remove(src.name);
        src.name = newName;
        src.parent = dstParent;
        dstParent.children.put(newName, src);
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.RENAME_DIR, normalize(getFullPath(src)), "renomeado de " + normalize(srcPath)));
        System.out.println("Diretório renomeado/movido para: " + normalize(getFullPath(src)));
    }

    public void listFiles(String path) {
        Node dir = traverseTo(path);
        if (dir == null) {
            System.out.println("Caminho não encontrado.");
            return;
        }
        if (dir.isFile()) {
            System.out.println("Caminho é um arquivo. Para ver conteúdo use ler arquivo.");
            return;
        }
        System.out.println("\n=== Conteúdo de " + (normalize(path).isEmpty() ? "/" : normalize(path)) + " ===");
        List<String> names = new ArrayList<>(dir.children.keySet());
        Collections.sort(names);
        for (String name : names) {
            Node child = dir.children.get(name);
            System.out.println((child.isDirectory() ? "[DIR] " : "[FILE] ") + name);
        }
    }

    // Read/Write content and move content between files

    public String readFile(String path) {
        Node node = traverseTo(path);
        if (node == null || node.isDirectory()) {
            System.out.println("Arquivo não encontrado.");
            return null;
        }
        return node.content;
    }

    public void writeFile(String path, String content, boolean overwrite) {
        Node node = traverseTo(path);
        if (node == null || node.isDirectory()) {
            System.out.println("Arquivo não encontrado.");
            return;
        }
        if (overwrite) node.content = content == null ? "" : content;
        else node.content = node.content + (content == null ? "" : content);
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.UPDATE_CONTENT, normalize(path), (overwrite ? "sobrescrito" : "acrescentado") + " len=" + (content == null ? 0 : content.length())));
        System.out.println("Arquivo atualizado: " + normalize(path));
    }

    public void moveContent(String srcPath, String dstPath, int count) {
        Node src = traverseTo(srcPath);
        Node dst = traverseTo(dstPath);
        if (src == null || src.isDirectory()) {
            System.out.println("Arquivo de origem inválido.");
            return;
        }
        if (dst == null || dst.isDirectory()) {
            System.out.println("Arquivo de destino inválido.");
            return;
        }
        String s = src.content;
        if (s.isEmpty()) {
            System.out.println("Arquivo de origem vazio.");
            return;
        }
        int moveCount = count <= 0 ? s.length() : Math.min(count, s.length());
        String moved = s.substring(0, moveCount);
        src.content = s.substring(moveCount);
        dst.content = dst.content + moved;
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.UPDATE_CONTENT, normalize(srcPath), "removidos " + moveCount + " chars para " + normalize(dstPath)));
        transactions.add(new TransactionEntry(TransactionEntry.OperationType.UPDATE_CONTENT, normalize(dstPath), "adicionados " + moveCount + " chars vindos de " + normalize(srcPath)));
        System.out.println("Movidos " + moveCount + " caracteres de " + normalize(srcPath) + " para " + normalize(dstPath));
    }

    // Helper to build full path of a node
    private String getFullPath(Node node) {
        if (node == null || node.parent == null) return "";
        Deque<String> parts = new ArrayDeque<>();
        Node cur = node;
        while (cur != null && cur.parent != null) {
            parts.addFirst(cur.name);
            cur = cur.parent;
        }
        return String.join("/", parts);
    }

    public void showTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("Nenhuma transação registrada.");
            return;
        }
        System.out.println("\n=== Transações ===");
        transactions.forEach(System.out::println);
    }
}