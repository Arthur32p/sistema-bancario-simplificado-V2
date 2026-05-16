package servidor;

import com.banco.protocolo.*;
import models.*;
import interfaces.ClienteService;
import interfaces.ContaService;

import java.io.IOException;
import java.util.List;

public class Dispatcher {
    public static byte[] processar(byte[] dadosEntrada, ContaService contaService, ClienteService clienteService) throws IOException {
        MensagemRMI request = MensagemRMI.parseFrom(dadosEntrada);

        int id = request.getRequestId();
        String objRef = request.getObjectReference();
        int op = Integer.parseInt(request.getMethodId());

        MensagemRMI.Builder reply = MensagemRMI.newBuilder()
                .setMessageType(1)
                .setRequestId(id);

        if (objRef.equals("ClienteService")) {
            switch (op) {

                // Cadastro
                case 1:
                    try {
                        CadastroRequest cadastroRequest = CadastroRequest.parseFrom(request.getArguments());

                        Cliente c = clienteService.salvarOuObter(cadastroRequest.getNome(), cadastroRequest.getCpf());
                        contaService.abrirConta(c, cadastroRequest.getSenha(), cadastroRequest.getTipo());

                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(0)
                                .build().toByteString());

                    } catch (IllegalArgumentException e) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-1)
                                .build().toByteString());
                    }
                    break;

                // Login
                case 2:
                    try {
                        LoginRequest loginRequest = LoginRequest.parseFrom(request.getArguments());

                        List<Conta> contas = clienteService.listarContas(loginRequest.getCpf());
                        Conta encontrada = null;

                        for (Conta c : contas) {
                            if (loginRequest.getTipo() == 1 && c instanceof ContaCorrente) {
                                if (c.getSenha().equals(loginRequest.getSenha())) {
                                    encontrada = c;
                                    break;
                                }
                            } else if (loginRequest.getTipo() == 2 && c instanceof ContaPoupanca) {
                                if (c.getSenha().equals(loginRequest.getSenha())) {
                                    encontrada = c;
                                    break;
                                }
                            }
                        }

                        if (encontrada != null) {
                            LoginResponse.Builder loginResp = LoginResponse.newBuilder()
                                    .setStatus(0)
                                    .setNumeroConta(encontrada.getNumero())
                                    .setNomeTitular(encontrada.getTitular().getNome())
                                    .setSaldo(encontrada.getSaldo());

                            if (encontrada instanceof ContaCorrente cc) {
                                loginResp.setLimite(cc.getLimite());
                                loginResp.setTipo(1);
                            } else {
                                loginResp.setRendimento(((ContaPoupanca) encontrada).getRendimento());
                                loginResp.setTipo(2);
                            }

                            reply.setArguments(loginResp.build().toByteString());
                        } else {
                            reply.setArguments(LoginResponse.newBuilder()
                                    .setStatus(-1)
                                    .build().toByteString());
                        }

                    } catch (IllegalArgumentException e) {
                        reply.setArguments(LoginResponse.newBuilder()
                                .setStatus(-2)
                                .build().toByteString());
                    }
                    break;
            }

        } else if (objRef.equals("ContaService")) {
            switch (op) {

                // Saque
                case 3:
                    SacarRequest sacarRequest = SacarRequest.parseFrom(request.getArguments());
                    Conta contaSaque = contaService.buscarConta(sacarRequest.getNumeroSaque());

                    if (contaSaque == null) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-1)
                                .build().toByteString());
                    } else if (contaService.sacar(contaSaque, sacarRequest.getValorSaque())) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(0)
                                .build().toByteString());
                    } else {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-2)
                                .build().toByteString());
                    }
                    break;

                // Deposito
                case 4:
                    DepositoRequest depositoRequest = DepositoRequest.parseFrom(request.getArguments());
                    Conta contaDeposito = contaService.buscarConta(depositoRequest.getNumeroDeposito());

                    if (contaDeposito == null) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-1)
                                .build().toByteString());
                    } else if (contaService.depositar(contaDeposito, depositoRequest.getValorDeposito())) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(0)
                                .build().toByteString());
                    } else {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-2)
                                .build().toByteString());
                    }
                    break;

                // Transferir
                case 5:
                    TransferirRequest transferirRequest = TransferirRequest.parseFrom(request.getArguments());

                    Conta origem = contaService.buscarConta(transferirRequest.getNumOrigem());
                    if (origem == null) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-1)
                                .build().toByteString());
                        break;
                    }

                    Conta destino = contaService.buscarConta(transferirRequest.getNumDestino());
                    if (destino == null) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-2)
                                .build().toByteString());
                        break;
                    }

                    if (contaService.transferir(origem, destino, transferirRequest.getValor())) {
                        reply.setArguments(TransferirResponse.newBuilder()
                                .setStatus(0)
                                .setNomeDestino(destino.getTitular().getNome())
                                .build().toByteString());
                    } else {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-3)
                                .build().toByteString());
                    }
                    break;

                // Pagar
                case 6:
                    PagarRequest pagarRequest = PagarRequest.parseFrom(request.getArguments());
                    Conta contaPag = contaService.buscarConta(pagarRequest.getNumContaPag());

                    if (contaPag == null) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-1)
                                .build().toByteString());
                        break;
                    }

                    if (contaService.pagar(contaPag, pagarRequest.getValorPag(), pagarRequest.getDescricao())) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(0)
                                .build().toByteString());
                    } else {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-2)
                                .build().toByteString());
                    }
                    break;

                // Projetar rendimento
                case 7:
                    ProjetarRendimentoRequest projetarRequest = ProjetarRendimentoRequest.parseFrom(request.getArguments());
                    Conta contaRendimento = contaService.buscarConta(projetarRequest.getNumeroRendimento());

                    if (contaRendimento instanceof ContaPoupanca cp) {
                        double resultado = contaService.projetarRendimento(cp, projetarRequest.getMeses());
                        reply.setArguments(RendimentoResponse.newBuilder()
                                .setStatus(0)
                                .setValorResultado(resultado)
                                .build().toByteString());
                    } else {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-1)
                                .build().toByteString());
                    }
                    break;

                // Extrato
                case 8:
                    ExtratoRequest extratoRequest = ExtratoRequest.parseFrom(request.getArguments());
                    Conta contaAlvo = contaService.buscarConta(extratoRequest.getNumContaExtrato());

                    if (contaAlvo == null) {
                        reply.setArguments(StatusResponse.newBuilder()
                                .setStatus(-1)
                                .build().toByteString());
                    } else {
                        List<String> historico = contaService.consultarExtrato(extratoRequest.getNumContaExtrato());
                        reply.setArguments(ExtratoResponse.newBuilder()
                                .setStatus(0)
                                .addAllLinhas(historico)
                                .build().toByteString());
                    }
                    break;
            }

        }

        return reply.build().toByteArray();

    }
}