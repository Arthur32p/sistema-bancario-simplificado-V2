package rmi;

import com.banco.protocolo.*;
import session.Sessao;

import java.rmi.RemoteException;

public class ServicoCliente {

    private static final int OP_CADASTRO = 1;
    private static final int OP_LOGIN    = 2;

    private final ConexaoRMI conexao;

    public ServicoCliente(ConexaoRMI conexao) {
        this.conexao = conexao;
    }


    public record ResultadoCadastro(boolean sucesso, String mensagem) {}
    public record ResultadoLogin(boolean sucesso, String mensagem, Sessao sessao) {}

    public ResultadoCadastro cadastrar(String nome, String cpf, String senha, int tipo) {
        try {
            CadastroRequest req = CadastroRequest.newBuilder()
                    .setNome(nome)
                    .setCpf(cpf)
                    .setSenha(senha)
                    .setTipo(tipo)
                    .build();

            byte[] resposta = conexao.doOperation("ClienteService", OP_CADASTRO, req.toByteArray());

            MensagemRMI reply  = MensagemRMI.parseFrom(resposta);
            StatusResponse status = StatusResponse.parseFrom(reply.getArguments());

            return switch (status.getStatus()) {
                case 0  -> new ResultadoCadastro(true,  "Conta aberta com sucesso!");
                case -1 -> new ResultadoCadastro(false, "CPF já possui esse tipo de conta.");
                default -> new ResultadoCadastro(false, "Erro desconhecido no servidor.");
            };

        } catch (RemoteException e) {
            return new ResultadoCadastro(false, "Erro de conexão: " + e.getMessage());
        } catch (Exception e) {
            return new ResultadoCadastro(false, "Erro ao processar resposta: " + e.getMessage());
        }
    }

    public ResultadoLogin login(String cpf, String senha, int tipo) {
        try {
            LoginRequest req = LoginRequest.newBuilder()
                    .setCpf(cpf)
                    .setSenha(senha)
                    .setTipo(tipo)
                    .build();

            byte[] resposta = conexao.doOperation("ClienteService", OP_LOGIN, req.toByteArray());

            MensagemRMI reply       = MensagemRMI.parseFrom(resposta);
            LoginResponse loginResp = LoginResponse.parseFrom(reply.getArguments());

            return switch (loginResp.getStatus()) {
                case 0  -> {
                    Sessao sessao = new Sessao(
                            loginResp.getNumeroConta(),
                            cpf,
                            loginResp.getNomeTitular(),
                            tipo,
                            loginResp.getSaldo(),
                            loginResp.getLimite(),
                            loginResp.getRendimento());
                    yield new ResultadoLogin(true, "Login realizado com sucesso!", sessao);
                }
                case -1 -> new ResultadoLogin(false, "CPF ou senha incorretos.", null);
                case -2 -> new ResultadoLogin(false, "Conta não encontrada.", null);
                default -> new ResultadoLogin(false, "Erro desconhecido no servidor.", null);
            };

        } catch (RemoteException e) {
            return new ResultadoLogin(false, "Erro de conexão: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ResultadoLogin(false, "Erro ao processar resposta: " + e.getMessage(), null);
        }
    }
}
