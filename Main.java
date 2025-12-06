import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando simulador de arquivos...");
        try {
            FileSystemSimulator simulator = new FileSystemSimulator();
            Scanner scanner = new Scanner(System.in);
            String currentDir = ""; // caminho relativo no simulador, "" = raiz

            loop:
            while (true) {
                System.out.print((currentDir.isEmpty() ? "/" : "/" + currentDir) + "> ");
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+", 3);
                String cmd = parts[0].toLowerCase();
                String arg1 = parts.length > 1 ? parts[1] : "";
                String arg2 = parts.length > 2 ? parts[2] : "";

                switch (cmd) {
                    case "help", "h" -> printHelp();
                    case "pwd" -> System.out.println("/" + (currentDir.isEmpty() ? "" : currentDir));
                    case "ls" -> {
                        String target = resolvePath(arg1, currentDir);
                        simulator.listFiles(target);
                    }
                    case "mkdir" -> {
                        if (arg1.isEmpty()) { System.out.println("Uso: mkdir <diretório>"); break; }
                        String target = resolvePath(arg1, currentDir);
                        simulator.createDirectory(target);
                    }
                    case "rmdir", "rm -r" -> {
                        if (arg1.isEmpty()) { System.out.println("Uso: rmdir <diretório>"); break; }
                        String target = resolvePath(arg1, currentDir);
                        simulator.deleteDirectory(target);
                    }
                    case "rm" -> {
                        if (arg1.isEmpty()) { System.out.println("Uso: rm <arquivo>"); break; }
                        String target = resolvePath(arg1, currentDir);
                        simulator.deleteFile(target);
                    }
                    case "touch" -> {
                        if (arg1.isEmpty()) { System.out.println("Uso: touch <arquivo>"); break; }
                        String target = resolvePath(arg1, currentDir);
                        simulator.createFile(target);
                    }
                    case "cat" -> {
                        if (arg1.isEmpty()) { System.out.println("Uso: cat <arquivo>"); break; }
                        String target = resolvePath(arg1, currentDir);
                        String content = simulator.readFile(target);
                        if (content != null) {
                            System.out.println("\n--- " + target + " ---");
                            System.out.println(content);
                            System.out.println("--- fim ---");
                        }
                    }
                    case "write" -> {
                        if (arg1.isEmpty()) { System.out.println("Uso: write <arquivo>"); break; }
                        String target = resolvePath(arg1, currentDir);
                        System.out.print("Conteúdo (uma linha): ");
                        String content = scanner.nextLine();
                        simulator.writeFile(target, content, true);
                    }
                    case "append" -> {
                        if (arg1.isEmpty()) { System.out.println("Uso: append <arquivo>"); break; }
                        String target = resolvePath(arg1, currentDir);
                        System.out.print("Conteúdo (uma linha): ");
                        String content = scanner.nextLine();
                        simulator.writeFile(target, content, false);
                    }
                    case "mv" -> {
                        if (arg1.isEmpty() || arg2.isEmpty()) { System.out.println("Uso: mv <origem> <destino>"); break; }
                        String src = resolvePath(arg1, currentDir);
                        String dst = resolvePath(arg2, currentDir);
                        // tenta renomear arquivo ou diretório (cada método trata seu tipo)
                        simulator.renameFile(src, dst);
                        simulator.renameDirectory(src, dst);
                    }
                    case "cp" -> {
                        if (arg1.isEmpty() || arg2.isEmpty()) { System.out.println("Uso: cp <origem> <destino>"); break; }
                        String src = resolvePath(arg1, currentDir);
                        String dst = resolvePath(arg2, currentDir);
                        simulator.copyFile(src, dst);
                    }
                    case "cd" -> {
                        String target = arg1.isEmpty() ? "" : resolvePath(arg1, currentDir);
                        // sem validação estrita: atualiza currentDir ("" = raiz)
                        currentDir = normalize(target);
                    }
                    case "transactions", "tx" -> simulator.showTransactions();
                    case "exit", "quit" -> {
                        System.out.println("Encerrando simulador...");
                        break loop;
                    }
                    default -> System.out.println("Comando desconhecido. Digite 'help' para ver os comandos.");
                }
            }

            scanner.close();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("""
            Comandos disponíveis:
              help                 - mostrar esta ajuda
              pwd                  - caminho atual
              ls [caminho]         - listar diretório (vazio = / ou dir atual)
              mkdir <dir>          - criar diretório
              rmdir <dir> | rm -r  - deletar diretório (recursivo)
              rm <arquivo>         - deletar arquivo
              touch <arquivo>      - criar arquivo vazio
              cat <arquivo>        - ver conteúdo
              write <arquivo>      - sobrescrever conteúdo (uma linha)
              append <arquivo>     - acrescentar conteúdo (uma linha)
              mv <origem> <destino>- mover/renomear arquivo ou diretório
              cp <origem> <destino>- copiar arquivo
              cd <caminho>         - mudar diretório (use / para raiz)
              transactions | tx    - ver transações registradas
              exit | quit          - sair
            """);
    }

    private static String resolvePath(String path, String currentDir) {
        if (path == null) return "";
        path = path.replace('\\', '/').trim();
        if (path.startsWith("/")) {
            return normalize(path.substring(1));
        }
        if (path.isEmpty()) return normalize(currentDir);
        if (currentDir == null || currentDir.isEmpty()) return normalize(path);
        return normalize(currentDir + "/" + path);
    }

    private static String normalize(String p) {
        if (p == null) return "";
        p = p.replace('\\', '/').trim();
        while (p.startsWith("/")) p = p.substring(1);
        while (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        // simplificado: não resolve .. ou .
        return p;
    }
}