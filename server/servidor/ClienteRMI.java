package servidor;

import com.banco.protocolo.*;
import interfaces.ServidorInterface;

import java.rmi.Naming;
import java.util.Scanner;

public class ClienteRMI {

    static ServidorInterface servidor;
    static Scanner scanner = new Scanner(System.in);
    static int numeroConta = -1;

    public static void main(String[] args) {
        try {
            servidor = (ServidorInterface) Naming.lookup("rmi://localhost/BancoService");
            System.out.println("Conectado ao servidor RMI!\n");
        } catch (Exception e) {
            System.out.println("Erro ao conectar no servidor: " + e.getMessage());
            return;
        }

        boolean rodando = true;
        while (rodando) {
            System.out.println("========== BANCO RMI ==========");
            System.out.println("1. Cadastrar conta");
            System.out.println("2. Login");
            System.out.println("0. Sair");
            System.out.print("Escolha: ");

            int opcao = lerInt();

            switch (opcao) {
                case 1 -> cadastrar();
                case 2 -> login();
                case 0 -> rodando = false;
                default -> System.out.println("Opcao invalida!\n");
            }
        }

        System.out.println("Ate logo!");
    }

    static void menuLogado() {
        boolean logado = true;
        while (logado) {
            System.out.println("\n========== MENU ==========");
            System.out.println("Conta: " + numeroConta);
            System.out.println("3. Sacar");
            System.out.println("4. Depositar");
            System.out.println("5. Transferir");
            System.out.println("6. Pagar");
            System.out.println("7. Projetar rendimento");
            System.out.println("8. Extrato");
            System.out.println("9. Logout");
            System.out.print("Escolha: ");

            int opcao = lerInt();
            switch (opcao) {
                case 3 -> sacar();
                case 4 -> depositar();
                case 5 -> transferir();
                case 6 -> pagar();
                case 7 -> projetarRendimento();
                case 8 -> extrato();
                case 9 -> {
                    numeroConta = -1;
                    logado = false;
                    System.out.println("Logout realizado!\n");
                }
                default -> System.out.println("Opcao invalida!\n");
            }
        }
    }

    // ─── Operacoes ───────────────────────────────────────────────────

    static void cadastrar() {
        try {
            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("CPF: ");
            String cpf = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();
            System.out.print("Tipo (1=Corrente, 2=Poupanca): ");
            int tipo = lerInt();

            byte[] pacote = montar("ClienteService", 1,
                    CadastroRequest.newBuilder()
                            .setNome(nome)
                            .setCpf(cpf)
                            .setSenha(senha)
                            .setTipo(tipo)
                            .build());

            byte[] resposta = doOperation(pacote);
            StatusResponse status = parseStatus(resposta);

            if (status.getStatus() == 0) {
                System.out.println("Conta criada com sucesso!\n");
            } else {
                System.out.println("Erro: conta deste tipo ja existe para este CPF.\n");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    static void login() {
        try {
            System.out.print("CPF: ");
            String cpf = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();
            System.out.print("Tipo (1=Corrente, 2=Poupanca): ");
            int tipo = lerInt();

            byte[] pacote = montar("ClienteService", 2,
                    LoginRequest.newBuilder()
                            .setCpf(cpf)
                            .setSenha(senha)
                            .setTipo(tipo)
                            .build());

            byte[] resposta = doOperation(pacote);
            LoginResponse login = parseLogin(resposta);

            if (login.getStatus() == 0) {
                numeroConta = login.getNumeroConta();
                System.out.println("\n========== LOGIN REALIZADO ==========");
                System.out.println("Titular  : " + login.getNomeTitular());
                System.out.println("Conta    : " + numeroConta);
                System.out.printf ("Saldo    : R$ %.2f%n", login.getSaldo());
                if (login.getTipo() == 1) {
                    System.out.printf("Limite   : R$ %.2f%n", login.getLimite());
                    System.out.println("Tipo     : Conta Corrente");
                } else {
                    System.out.printf("Rendimento: %.2f%% a.m.%n", login.getRendimento() * 100);
                    System.out.println("Tipo     : Conta Poupanca");
                }
                System.out.println("=====================================\n");

                menuLogado(); // entra no submenu
            } else {
                System.out.println("CPF, senha ou tipo incorretos.\n");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    static void sacar() {
        try {
            System.out.print("Valor para saque: R$ ");
            double valor = lerDouble();

            byte[] pacote = montar("ContaService", 3,
                    SacarRequest.newBuilder()
                            .setNumeroSaque(numeroConta)
                            .setValorSaque(valor)
                            .build());

            byte[] resposta = doOperation(pacote);
            StatusResponse status = parseStatus(resposta);

            if (status.getStatus() == 0) {
                System.out.println("Saque de R$ " + valor + " realizado!\n");
            } else {
                System.out.println("Saldo insuficiente.\n");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    static void depositar() {
        try {
            System.out.print("Valor para deposito: R$ ");
            double valor = lerDouble();

            byte[] pacote = montar("ContaService", 4,
                    DepositoRequest.newBuilder()
                            .setNumeroDeposito(numeroConta)
                            .setValorDeposito(valor)
                            .build());

            byte[] resposta = doOperation(pacote);
            StatusResponse status = parseStatus(resposta);

            if (status.getStatus() == 0) {
                System.out.println("Deposito de R$ " + valor + " realizado!\n");
            } else {
                System.out.println("Erro ao depositar.\n");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    static void transferir() {
        try {
            System.out.print("Numero da conta destino: ");
            int destino = lerInt();
            System.out.print("Valor: R$ ");
            double valor = lerDouble();

            byte[] pacote = montar("ContaService", 5,
                    TransferirRequest.newBuilder()
                            .setNumOrigem(numeroConta)
                            .setNumDestino(destino)
                            .setValor(valor)
                            .build());

            byte[] resposta = doOperation(pacote);
            MensagemRMI reply = MensagemRMI.parseFrom(resposta);
            StatusResponse st = StatusResponse.parseFrom(reply.getArguments());

            if (st.getStatus() == 0) {
                TransferirResponse tr = TransferirResponse.parseFrom(reply.getArguments());
                System.out.println("Transferencia de R$ " + valor + " para " + tr.getNomeDestino() + " realizada!\n");
            } else if (st.getStatus() == -1) {
                System.out.println("Conta origem nao encontrada.\n");
            } else if (st.getStatus() == -2) {
                System.out.println("Conta destino nao encontrada.\n");
            } else {
                System.out.println("Saldo insuficiente.\n");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    static void pagar() {
        try {
            System.out.print("Valor: R$ ");
            double valor = lerDouble();
            System.out.print("Descricao: ");
            String descricao = scanner.nextLine();

            byte[] pacote = montar("ContaService", 6,
                    PagarRequest.newBuilder()
                            .setNumContaPag(numeroConta)
                            .setValorPag(valor)
                            .setDescricao(descricao)
                            .build());

            byte[] resposta = doOperation(pacote);
            StatusResponse status = parseStatus(resposta);

            if (status.getStatus() == 0) {
                System.out.println("Pagamento de R$ " + valor + " realizado!\n");
            } else if (status.getStatus() == -1) {
                System.out.println("Conta nao encontrada.\n");
            } else {
                System.out.println("Saldo insuficiente ou conta nao e corrente.\n");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    static void projetarRendimento() {
        try {
            System.out.print("Quantos meses projetar? ");
            int meses = lerInt();

            byte[] pacote = montar("ContaService", 7,
                    ProjetarRendimentoRequest.newBuilder()
                            .setNumeroRendimento(numeroConta)
                            .setMeses(meses)
                            .build());

            byte[] resposta = doOperation(pacote);
            MensagemRMI reply = MensagemRMI.parseFrom(resposta);
            RendimentoResponse rendimento = RendimentoResponse.parseFrom(reply.getArguments());

            if (rendimento.getStatus() == 0) {
                System.out.printf("Rendimento projetado em %d meses: R$ %.2f%n%n",
                        meses, rendimento.getValorResultado());
            } else {
                System.out.println("Conta nao e poupanca ou nao encontrada.\n");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    static void extrato() {
        try {
            byte[] pacote = montar("ContaService", 8,
                    ExtratoRequest.newBuilder()
                            .setNumContaExtrato(numeroConta)
                            .build());

            byte[] resposta = doOperation(pacote);
            MensagemRMI reply = MensagemRMI.parseFrom(resposta);
            ExtratoResponse extrato = ExtratoResponse.parseFrom(reply.getArguments());

            if (extrato.getStatus() == 0) {
                System.out.println("===== EXTRATO DA CONTA " + numeroConta + " =====");
                if (extrato.getLinhasCount() == 0) {
                    System.out.println("Sem movimentacoes.");
                } else {
                    for (String linha : extrato.getLinhasList()) {
                        System.out.println("  " + linha);
                    }
                }
                System.out.println();
            } else {
                System.out.println("Conta nao encontrada.\n");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ─── Protocolo requisicao-resposta ───────────────────────────────

    static byte[] doOperation(byte[] pacote) throws Exception {
        return servidor.processarOperacao(pacote);
    }

    // ─── Helpers ────────────────────────────────────────────────────

    static byte[] montar(String objRef, int op, com.google.protobuf.Message args) throws Exception {
        return MensagemRMI.newBuilder()
                .setMessageType(0)
                .setRequestId(1)
                .setObjectReference(objRef)
                .setMethodId(String.valueOf(op))
                .setArguments(args.toByteString())
                .build()
                .toByteArray();
    }

    static StatusResponse parseStatus(byte[] resposta) throws Exception {
        MensagemRMI reply = MensagemRMI.parseFrom(resposta);
        return StatusResponse.parseFrom(reply.getArguments());
    }

    static LoginResponse parseLogin(byte[] resposta) throws Exception {
        MensagemRMI reply = MensagemRMI.parseFrom(resposta);
        return LoginResponse.parseFrom(reply.getArguments());
    }

    static int lerInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    static double lerDouble() {
        try {
            return Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }
}