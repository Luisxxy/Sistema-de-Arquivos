# Simulador de Sistema de Arquivos (Java)

Resumo rápido do projeto e como usar.

## Visão geral
O simulador implementa um sistema de arquivos em memória em Java com suporte a arquivos e diretórios e registro de transações (classe `TransactionEntry`). A interface principal é uma linha de comando (arquivo `Main.java`) que aceita comandos estilo shell.

## Arquivos principais
- `Main.java` — shell interativo; interpreta comandos do usuário e chama `FileSystemSimulator`.
- `FileSystemSimulator.java` — lógica do sistema de arquivos (criar/apagar/renomear/copy arquivos e diretórios, ler/escrever conteúdo, listar).
- `TransactionEntry.java` — DTO que registra operações com timestamp e tipo.

## Como compilar (Windows)
1. Abra o terminal integrado no VS Code ou PowerShell.
2. Navegue até a pasta do projeto:
   cd "e:\OneDrive\Desktop\Simulador de Arquivos"
3. Compile:
   javac *.java
4. Rode:
   java Main

## Comandos suportados (uso na linha de comando do simulador)
- help — mostra ajuda
- pwd — mostra caminho atual
- ls [caminho] — listar diretório (vazio = diretório atual / raiz)
- mkdir <dir> — criar diretório
- rmdir <dir> | rm -r <dir> — deletar diretório (recursivo)
- rm <arquivo> — deletar arquivo
- touch <arquivo> — criar arquivo vazio
- cat <arquivo> — mostrar conteúdo do arquivo
- write <arquivo> — sobrescrever conteúdo (uma linha)
- append <arquivo> — acrescentar conteúdo (uma linha)
- mv <origem> <destino> — mover/renomear arquivo ou diretório
- cp <origem> <destino> — copiar arquivo
- cd <caminho> — mudar diretório (`/` para raiz)
- transactions | tx — mostrar log de transações
- exit | quit — sair do simulador

Exemplo:
- touch docs/nota.txt
- write docs/nota.txt  (será pedido o conteúdo)
- cat docs/nota.txt
- cp docs/nota.txt backup/
- mv docs/nota.txt docs/nota_old.txt
- rm backup/nota.txt

## Observações e limitações
- Caminhos usam `/` (barra). Backslash é convertido para `/`.
- O normalizador atual não resolve `..` ou `.`.
- Ao criar um arquivo, diretórios pai são criados automaticamente quando necessário.
- `deleteDirectory` atualmente é recursivo (apaga todo o conteúdo).
- Transações registram operações (tipo, timestamp, caminho afetado e payload resumido).
- O simulador guarda tudo em memória — não altera o sistema de arquivos real.

## Próximos passos sugeridos (opcional)
- Suportar `..` e `.` em paths.
- Fazer `rmdir` falhar se diretório não estiver vazio (opção não recursiva).
- Suporte a edição multi-linha, salvar/abrir em disco, ou UI gráfica/web.

Se quiser, eu gero o README como arquivo no diretório ou adiciono detalhes extras (exemplos de sessão).  