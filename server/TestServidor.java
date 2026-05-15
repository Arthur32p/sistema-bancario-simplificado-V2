import com.banco.protocolo.*;
import models.*;
import service.ClienteServiceImpl;
import service.ContaServiceImpl;
import servidor.Dispatcher;

/**
 * Testes manuais do servidor.Dispatcher — roda sem JUnit, só com Java puro.
 * Execute direto com: java TestServidor
 */
public class TestServidor {

    static ContaServiceImpl contaService;
    static ClienteServiceImpl clienteService;

    static int passou = 0;
    static int falhou = 0;

    public static void main(String[] args) throws Exception {
        Banco banco = new Banco();
        contaService = new ContaServiceImpl(banco);
        clienteService = new ClienteServiceImpl(banco);

        System.out.println("========== TESTES DO SERVIDOR ==========\n");

        testarCadastro();
        testarCadastroDuplicado();
        testarLogin();
        testarLoginSenhaErrada();
        testarDeposito();
        testarDepositoContaInexistente();
        testarSaque();
        testarSaqueSaldoInsuficiente();
        testarTransferencia();
        testarPagamento();
        testarExtrato();
        testarRendimento();

        System.out.println("\n========== RESULTADO ==========");
        System.out.println("✅ Passou: " + passou);
        System.out.println("❌ Falhou: " + falhou);
        System.out.println("================================");
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

    static void checar(String nome, boolean condicao) {
        if (condicao) {
            System.out.println("✅ " + nome);
            passou++;
        } else {
            System.out.println("❌ " + nome + " — FALHOU");
            falhou++;
        }
    }

    // ─── Testes ─────────────────────────────────────────────────────

    static int numeroConta = -1; // salva o numero da conta criada

    static void testarCadastro() throws Exception {
        byte[] pacote = montar("ClienteService", 1,
                CadastroRequest.newBuilder()
                        .setNome("Joao Silva")
                        .setCpf("111.111.111-11")
                        .setSenha("1234")
                        .setTipo(1) // ContaCorrente
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        StatusResponse status = parseStatus(resposta);

        checar("Cadastro com sucesso → status 0", status.getStatus() == 0);

        // ContaCorrente começa em 1000
        Conta c = contaService.buscarConta(1000);
        if (c != null) numeroConta = c.getNumero();
    }

    static void testarCadastroDuplicado() throws Exception {
        byte[] pacote = montar("ClienteService", 1,
                CadastroRequest.newBuilder()
                        .setNome("Joao Silva")
                        .setCpf("111.111.111-11")
                        .setSenha("1234")
                        .setTipo(1) // mesmo tipo → deve falhar
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        StatusResponse status = parseStatus(resposta);

        checar("Cadastro duplicado → status -1", status.getStatus() == -1);
    }

    static void testarLogin() throws Exception {
        byte[] pacote = montar("ClienteService", 2,
                LoginRequest.newBuilder()
                        .setCpf("111.111.111-11")
                        .setSenha("1234")
                        .setTipo(1)
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        LoginResponse login = parseLogin(resposta);

        checar("Login com sucesso → status 0", login.getStatus() == 0);
        checar("Login retorna numeroConta válido", login.getNumeroConta() > 0);
    }

    static void testarLoginSenhaErrada() throws Exception {
        byte[] pacote = montar("ClienteService", 2,
                LoginRequest.newBuilder()
                        .setCpf("111.111.111-11")
                        .setSenha("senhaerrada")
                        .setTipo(1)
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        LoginResponse login = parseLogin(resposta);

        checar("Login senha errada → status -1", login.getStatus() == -1);
    }

    static void testarDeposito() throws Exception {
        byte[] pacote = montar("ContaService", 4,
                DepositoRequest.newBuilder()
                        .setNumeroDeposito(numeroConta)
                        .setValorDeposito(500.0)
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        StatusResponse status = parseStatus(resposta);

        checar("Deposito R$500 → status 0", status.getStatus() == 0);
    }

    static void testarDepositoContaInexistente() throws Exception {
        byte[] pacote = montar("ContaService", 4,
                DepositoRequest.newBuilder()
                        .setNumeroDeposito(9999)
                        .setValorDeposito(100.0)
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        StatusResponse status = parseStatus(resposta);

        checar("Deposito conta inexistente → status -1", status.getStatus() == -1);
    }

    static void testarSaque() throws Exception {
        byte[] pacote = montar("ContaService", 3,
                SacarRequest.newBuilder()
                        .setNumeroSaque(numeroConta)
                        .setValorSaque(100.0)
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        StatusResponse status = parseStatus(resposta);

        checar("Saque R$100 → status 0", status.getStatus() == 0);
    }

    static void testarSaqueSaldoInsuficiente() throws Exception {
        byte[] pacote = montar("ContaService", 3,
                SacarRequest.newBuilder()
                        .setNumeroSaque(numeroConta)
                        .setValorSaque(99999.0) // muito acima do saldo
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        StatusResponse status = parseStatus(resposta);

        checar("Saque saldo insuficiente → status -2", status.getStatus() == -2);
    }

    static void testarTransferencia() throws Exception {
        // Cria segunda conta pra receber
        byte[] cadastro2 = montar("ClienteService", 1,
                CadastroRequest.newBuilder()
                        .setNome("Maria Souza")
                        .setCpf("222.222.222-22")
                        .setSenha("abcd")
                        .setTipo(1)
                        .build());
        Dispatcher.processar(cadastro2, contaService, clienteService);

        Conta destino = contaService.buscarConta(1001); // segunda ContaCorrente
        if (destino == null) {
            checar("Transferencia → conta destino criada", false);
            checar("Transferencia R$50 → status 0", false);
            return;
        }

        byte[] pacote = montar("ContaService", 5,
                TransferirRequest.newBuilder()
                        .setNumOrigem(numeroConta)
                        .setNumDestino(destino.getNumero())
                        .setValor(50.0)
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        MensagemRMI reply = MensagemRMI.parseFrom(resposta);
        TransferirResponse transferir = TransferirResponse.parseFrom(reply.getArguments());

        checar("Transferencia R$50 → status 0", transferir.getStatus() == 0);
        checar("Transferencia retorna nome do destino", !transferir.getNomeDestino().isEmpty());
    }

    static void testarPagamento() throws Exception {
        byte[] pacote = montar("ContaService", 6,
                PagarRequest.newBuilder()
                        .setNumContaPag(numeroConta)
                        .setValorPag(50.0)
                        .setDescricao("Conta de luz")
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        StatusResponse status = parseStatus(resposta);

        checar("Pagamento R$50 → status 0", status.getStatus() == 0);
    }

    static void testarExtrato() throws Exception {
        byte[] pacote = montar("ContaService", 8,
                ExtratoRequest.newBuilder()
                        .setNumContaExtrato(numeroConta)
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        MensagemRMI reply = MensagemRMI.parseFrom(resposta);
        ExtratoResponse extrato = ExtratoResponse.parseFrom(reply.getArguments());

        checar("Extrato → status 0", extrato.getStatus() == 0);
        checar("Extrato tem entradas", extrato.getLinhasCount() > 0);
    }

    static void testarRendimento() throws Exception {
        // Cria uma conta poupança pra testar rendimento
        byte[] cadastro = montar("ClienteService", 1,
                CadastroRequest.newBuilder()
                        .setNome("Carlos Poupador")
                        .setCpf("333.333.333-33")
                        .setSenha("poup")
                        .setTipo(2) // ContaPoupanca
                        .build());
        Dispatcher.processar(cadastro, contaService, clienteService);

        // ContaPoupanca começa em 5000
        Conta contaPoup = contaService.buscarConta(5000);
        if (contaPoup == null) {
            checar("Rendimento → conta poupança criada", false);
            checar("Rendimento → valor projetado > 0", false);
            return;
        }

        contaService.depositar(contaPoup, 1000.0);

        byte[] pacote = montar("ContaService", 7,
                ProjetarRendimentoRequest.newBuilder()
                        .setNumeroRendimento(contaPoup.getNumero())
                        .setMeses(12)
                        .build());

        byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
        MensagemRMI reply = MensagemRMI.parseFrom(resposta);
        RendimentoResponse rendimento = RendimentoResponse.parseFrom(reply.getArguments());

        checar("Rendimento → status 0", rendimento.getStatus() == 0);
        checar("Rendimento → valor projetado > 1000", rendimento.getValorResultado() > 1000.0);
    }
}