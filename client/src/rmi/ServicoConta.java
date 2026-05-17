package rmi;

import com.banco.protocolo.*;

import java.rmi.RemoteException;
import java.util.List;


public class ServicoConta {

    private static final int OP_SACAR              = 3;
    private static final int OP_DEPOSITAR          = 4;
    private static final int OP_TRANSFERIR         = 5;
    private static final int OP_PAGAR              = 6;
    private static final int OP_PROJETAR_RENDIMENTO = 7;
    private static final int OP_EXTRATO            = 8;

    private final ConexaoRMI conexao;

    public ServicoConta(ConexaoRMI conexao) {
        this.conexao = conexao;
    }


    public enum StatusOp { OK, CONTA_INVALIDA, SALDO_INSUFICIENTE, ERRO }

    public record ResultadoSimples(StatusOp status, String mensagem) {
        public boolean ok() { return status == StatusOp.OK; }
    }

    public record ResultadoTransferencia(StatusOp status, String mensagem, String nomeDestino) {
        public boolean ok() { return status == StatusOp.OK; }
    }

    public record ResultadoRendimento(StatusOp status, String mensagem, double valor) {
        public boolean ok() { return status == StatusOp.OK; }
    }

    public record ResultadoExtrato(StatusOp status, String mensagem, List<String> linhas) {
        public boolean ok() { return status == StatusOp.OK; }
    }

    public ResultadoSimples depositar(int numeroConta, double valor) {
        try {
            DepositoRequest req = DepositoRequest.newBuilder()
                    .setNumeroDeposito(numeroConta)
                    .setValorDeposito(valor)
                    .build();

            byte[] resposta = conexao.doOperation("ContaService", OP_DEPOSITAR, req.toByteArray());
            return parsarStatusSimples(resposta,
                    "Depósito realizado com sucesso.",
                    "Conta não encontrada.",
                    "Valor inválido para depósito.");

        } catch (Exception e) {
            return new ResultadoSimples(StatusOp.ERRO, "Erro de comunicação: " + e.getMessage());
        }
    }

    public ResultadoSimples sacar(int numeroConta, double valor) {
        try {
            SacarRequest req = SacarRequest.newBuilder()
                    .setNumeroSaque(numeroConta)
                    .setValorSaque(valor)
                    .build();

            byte[] resposta = conexao.doOperation("ContaService", OP_SACAR, req.toByteArray());
            return parsarStatusSimples(resposta,
                    "Saque realizado com sucesso.",
                    "Conta não encontrada.",
                    "Saldo ou limite insuficiente.");

        } catch (Exception e) {
            return new ResultadoSimples(StatusOp.ERRO, "Erro de comunicação: " + e.getMessage());
        }
    }

    public ResultadoTransferencia transferir(int numOrigem, int numDestino, double valor) {
        try {
            TransferirRequest req = TransferirRequest.newBuilder()
                    .setNumOrigem(numOrigem)
                    .setNumDestino(numDestino)
                    .setValor(valor)
                    .build();

            byte[] resposta = conexao.doOperation("ContaService", OP_TRANSFERIR, req.toByteArray());
            MensagemRMI reply = MensagemRMI.parseFrom(resposta);

            try {
                TransferirResponse tr = TransferirResponse.parseFrom(reply.getArguments());
                if (tr.getStatus() == 0) {
                    return new ResultadoTransferencia(StatusOp.OK,
                            "Transferência realizada com sucesso.", tr.getNomeDestino());
                }
            } catch (Exception ignored) {}

            StatusResponse sr = StatusResponse.parseFrom(reply.getArguments());
            return switch (sr.getStatus()) {
                case -1 -> new ResultadoTransferencia(StatusOp.CONTA_INVALIDA,
                        "Conta de origem não encontrada.", "");
                case -2 -> new ResultadoTransferencia(StatusOp.CONTA_INVALIDA,
                        "Conta de destino não encontrada.", "");
                case -3 -> new ResultadoTransferencia(StatusOp.SALDO_INSUFICIENTE,
                        "Saldo ou limite insuficiente.", "");
                default -> new ResultadoTransferencia(StatusOp.ERRO,
                        "Erro desconhecido no servidor.", "");
            };

        } catch (Exception e) {
            return new ResultadoTransferencia(StatusOp.ERRO,
                    "Erro de comunicação: " + e.getMessage(), "");
        }
    }

    public ResultadoSimples pagar(int numeroConta, double valor, String descricao) {
        try {
            PagarRequest req = PagarRequest.newBuilder()
                    .setNumContaPag(numeroConta)
                    .setValorPag(valor)
                    .setDescricao(descricao)
                    .build();

            byte[] resposta = conexao.doOperation("ContaService", OP_PAGAR, req.toByteArray());
            return parsarStatusSimples(resposta,
                    "Pagamento realizado com sucesso.",
                    "Conta não encontrada.",
                    "Saldo ou limite insuficiente.");

        } catch (Exception e) {
            return new ResultadoSimples(StatusOp.ERRO, "Erro de comunicação: " + e.getMessage());
        }
    }

    public ResultadoRendimento projetarRendimento(int numeroConta, int meses) {
        try {
            ProjetarRendimentoRequest req = ProjetarRendimentoRequest.newBuilder()
                    .setNumeroRendimento(numeroConta)
                    .setMeses(meses)
                    .build();

            byte[] resposta = conexao.doOperation("ContaService", OP_PROJETAR_RENDIMENTO, req.toByteArray());
            MensagemRMI reply = MensagemRMI.parseFrom(resposta);

            try {
                RendimentoResponse rr = RendimentoResponse.parseFrom(reply.getArguments());
                if (rr.getStatus() == 0) {
                    return new ResultadoRendimento(StatusOp.OK, "Projeção calculada.", rr.getValorResultado());
                }
            } catch (Exception ignored) {}

            return new ResultadoRendimento(StatusOp.ERRO,
                    "Conta não é do tipo poupança ou não encontrada.", 0);

        } catch (Exception e) {
            return new ResultadoRendimento(StatusOp.ERRO,
                    "Erro de comunicação: " + e.getMessage(), 0);
        }
    }

    public ResultadoExtrato extrato(int numeroConta) {
        try {
            ExtratoRequest req = ExtratoRequest.newBuilder()
                    .setNumContaExtrato(numeroConta)
                    .build();

            byte[] resposta = conexao.doOperation("ContaService", OP_EXTRATO, req.toByteArray());
            MensagemRMI reply = MensagemRMI.parseFrom(resposta);

            try {
                ExtratoResponse er = ExtratoResponse.parseFrom(reply.getArguments());
                if (er.getStatus() == 0) {
                    return new ResultadoExtrato(StatusOp.OK, "Extrato obtido.", er.getLinhasList());
                }
            } catch (Exception ignored) {}

            return new ResultadoExtrato(StatusOp.CONTA_INVALIDA, "Conta não encontrada.", List.of());

        } catch (Exception e) {
            return new ResultadoExtrato(StatusOp.ERRO,
                    "Erro de comunicação: " + e.getMessage(), List.of());
        }
    }

    private ResultadoSimples parsarStatusSimples(byte[] resposta,
            String msgOk, String msgContaInvalida, String msgSaldoInsuf)
            throws Exception {
        MensagemRMI reply  = MensagemRMI.parseFrom(resposta);
        StatusResponse sr  = StatusResponse.parseFrom(reply.getArguments());
        return switch (sr.getStatus()) {
            case 0  -> new ResultadoSimples(StatusOp.OK,               msgOk);
            case -1 -> new ResultadoSimples(StatusOp.CONTA_INVALIDA,   msgContaInvalida);
            case -2 -> new ResultadoSimples(StatusOp.SALDO_INSUFICIENTE, msgSaldoInsuf);
            default -> new ResultadoSimples(StatusOp.ERRO,             "Erro inesperado no servidor.");
        };
    }
}
