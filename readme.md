# Sistema Bancário Simplificado — RMI + Protobuf

**Trabalho 2 — Sistemas Distribuídos (QXD0043)**  
Universidade Federal do Ceará — Campus Quixadá  
Professor: Rafael Braga

---

## Descrição

Sistema bancário cliente-servidor implementado com **Java RMI** (Remote Method Invocation) e serialização via **Protocol Buffers (Protobuf)**. O cliente opera inteiramente no terminal e se comunica com o servidor através de invocação remota de métodos, sem uso de sockets explícitos.

---

## Estrutura do repositório

```
sistema-bancario-simplificado-V2/
├── server/                          ← Servidor RMI
│   ├── com/banco/protocolo/         ← Classes geradas pelo Protobuf
│   ├── interfaces/                  ← Interfaces remotas (ServidorInterface, ContaService, ClienteService)
│   ├── models/                      ← Entidades: Banco, Cliente, Conta, ContaCorrente, ContaPoupanca
│   ├── service/                     ← Implementações: ContaServiceImpl, ClienteServiceImpl
│   ├── servidor/                    ← ServidorRMI (main), Dispatcher
│   ├── proto/                       ← MensagemRMI.proto (definição do protocolo)
│   ├── protobuf-java-4.34.1.jar     ← Dependência do Protobuf
│   └── TestServidor.java            ← Testes manuais do servidor
│
└── client/                          ← Cliente RMI (terminal)
    └── src/
        ├── ClienteApp.java          ← Ponto de entrada, menu principal
        ├── ui/
        │   ├── Banner.java          ← UI: cores ANSI, logo ASCII, layout
        │   ├── TelaLogin.java       ← Tela de autenticação
        │   ├── TelaCadastro.java    ← Tela de abertura de conta
        │   └── TelaConta.java       ← Dashboard + operações bancárias
        ├── rmi/
        │   ├── ConexaoRMI.java      ← Implementa doOperation() — protocolo seção 5.2
        │   ├── ServicoCliente.java  ← Cadastro e login
        │   └── ServicoConta.java    ← Saque, depósito, transferência, pagamento, extrato, rendimento
        └── session/
            └── Sessao.java          ← Dados da sessão do usuário logado
```

---

## Requisitos

- **Java 17+** (usa records e switch expressions)
- IntelliJ IDEA ou terminal com `javac`/`java`
- Sem dependências externas além do `protobuf-java-4.34.1.jar` já incluso

---

## Como rodar

### No IntelliJ IDEA

**Configuração única (primeira vez):**

1. Abra a pasta `server` como projeto no IntelliJ
2. `File → Project Structure → Modules → Dependencies → +` → adicione `protobuf-java-4.34.1.jar`
3. Compile: `Build → Build Project`
4. Abra a pasta `client` em outra janela do IntelliJ
5. `File → Project Structure → Modules → Dependencies → +` → adicione:
   - `server/protobuf-java-4.34.1.jar`
   - Pasta `server/out/production/server` (classes compiladas do servidor)
   - Se não funcionar, edite `client/client.iml` conforme descrito abaixo

**Rodando:**
1. Execute `servidor.ServidorRMI` no projeto do servidor
2. Execute `ClienteApp` no projeto do cliente

> ⚠️ No IntelliJ o `System.console()` retorna `null`, então a senha aparece visível e o clear de tela não funciona. Para a experiência completa, use o terminal.

### No terminal

**Servidor** — abra um terminal e rode:

```cmd
cd sistema-bancario-simplificado-V2\server

mkdir out
javac -cp ".;protobuf-java-4.34.1.jar" -sourcepath "." -d out models\*.java interfaces\*.java service\*.java servidor\*.java com\banco\protocolo\*.java

java -cp "out;protobuf-java-4.34.1.jar" servidor.ServidorRMI
```

**Cliente** — abra outro terminal e rode:

```cmd
cd sistema-bancario-simplificado-V2\client

mkdir out
javac -cp ".;..\server\out;..\server\protobuf-java-4.34.1.jar" -sourcepath src -d out src\ClienteApp.java

java -cp "out;..\server\out;..\server\protobuf-java-4.34.1.jar" ClienteApp
```

Para conectar em servidor remoto:
```cmd
java -cp "out;..\server\out;..\server\protobuf-java-4.34.1.jar" ClienteApp 192.168.1.10
```

---

## Protocolo implementado

Segue o protocolo **requisição-resposta** descrito na seção 5.2 do livro texto (Coulouris et al.), com mensagens empacotadas via **Protocol Buffers**:

```
┌──────────────┬───────────┬─────────────────┬──────────┬───────────┐
│ messageType  │ requestId │ objectReference │ methodId │ arguments │
└──────────────┴───────────┴─────────────────┴──────────┴───────────┘
```

O método `doOperation()` em `ConexaoRMI` monta essa mensagem e invoca `servidor.processarOperacao()` via RMI, sem sockets explícitos. O `Dispatcher` no servidor desmonta a mensagem e despacha para o service correto.

### Mapeamento de operações

| methodId | objectReference | Operação              |
|----------|-----------------|-----------------------|
| 1        | ClienteService  | Cadastro              |
| 2        | ClienteService  | Login                 |
| 3        | ContaService    | Saque                 |
| 4        | ContaService    | Depósito              |
| 5        | ContaService    | Transferência         |
| 6        | ContaService    | Pagamento de boleto   |
| 7        | ContaService    | Projetar rendimento   |
| 8        | ContaService    | Extrato               |

---

## Modelo de domínio

```
Banco
 ├── tem-um → List<Cliente>     (agregação)
 └── tem-um → List<Conta>       (agregação)

Cliente
 └── tem-um → String cpf, nome

Conta  (abstrata)
 ├── tem-um → Cliente titular   (agregação)
 ├── é-uma  → ContaCorrente     (extensão) — tem limite e imposto
 └── é-uma  → ContaPoupanca     (extensão) — tem rendimento mensal
```

**Hierarquia de interfaces:**
```
ServidorInterface  (Remote)
ContaService       (Remote) → implementado por ContaServiceImpl
ClienteService     (Remote) → implementado por ClienteServiceImpl
Tributavel                  → implementado por ContaCorrente
```

---

## Funcionalidades

| Funcionalidade        | Conta Corrente | Conta Poupança |
|-----------------------|:--------------:|:--------------:|
| Depósito              | ✅             | ✅             |
| Saque                 | ✅             | ✅             |
| Transferência         | ✅             | ✅             |
| Extrato               | ✅             | ✅             |
| Pagar boleto          | ✅             | ❌             |
| Projetar rendimento   | ❌             | ✅             |

---

## Observações técnicas

- **Passagem por referência:** objetos remotos (`ContaService`, `ClienteService`) são acessados via stub RMI — passagem por referência remota
- **Passagem por valor:** argumentos e resultados são serializados com Protobuf (representação externa de dados) — passagem por valor
- **Concorrência:** métodos de serviço são `synchronized` para acesso seguro em ambiente multi-cliente
- **Numeração de contas:** ContaCorrente inicia em 1000, ContaPoupança inicia em 5000
