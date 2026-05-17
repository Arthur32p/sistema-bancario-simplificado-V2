package rmi;

import interfaces.ServidorInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Encapsula a conexão com o servidor RMI.
 *
 * Expõe doOperation() conforme o protocolo requisição-resposta
 * descrito no livro (seção 5.2), delegando ao servidor remoto.
 *
 * Não usa sockets diretamente — toda comunicação é via RMI.
 */
public class ConexaoRMI {

    private final ServidorInterface servidor;
    private int requestIdCounter = 0;

    /**
     * Estabelece a conexão com o servidor RMI.
     *
     * @param host endereço do servidor
     * @param porta porta do RMI registry
     */
    public ConexaoRMI(String host, int porta) throws Exception {
        String url = "rmi://" + host + ":" + porta + "/BancoService";
        this.servidor = (ServidorInterface) Naming.lookup(url);
    }

    /**
     * Implementação do protocolo requisição-resposta (seção 5.2 do livro).
     *
     * Envia uma mensagem de requisição para o objeto remoto e retorna a resposta.
     *
     * @param objectReference nome do objeto remoto ("ClienteService" ou "ContaService")
     * @param methodId        id do método a invocar
     * @param arguments       argumentos serializados via Protobuf
     * @return resposta serializada via Protobuf
     */
    public byte[] doOperation(String objectReference, int methodId, byte[] arguments)
            throws RemoteException {
        int reqId = ++requestIdCounter;

        try {
            // Monta a mensagem RMI conforme o protocolo
            com.banco.protocolo.MensagemRMI mensagem =
                    com.banco.protocolo.MensagemRMI.newBuilder()
                            .setMessageType(0)          // 0 = REQUEST
                            .setRequestId(reqId)
                            .setObjectReference(objectReference)
                            .setMethodId(String.valueOf(methodId))
                            .setArguments(
                                    com.google.protobuf.ByteString.copyFrom(arguments))
                            .build();

            // Invocação remota de método — sem sockets explícitos
            return servidor.processarOperacao(mensagem.toByteArray());

        } catch (Exception e) {
            throw new RemoteException("Falha em doOperation: " + e.getMessage(), e);
        }
    }

    /**
     * Encerra a conexão (RMI não exige fechamento explícito, mas
     * mantemos por simetria com o protocolo do trabalho).
     */
    public void fechar() {
        // Nada a fechar em RMI puro — o registry cuida da limpeza
    }
}
