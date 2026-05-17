package rmi;

import interfaces.ServidorInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;

public class ConexaoRMI {

    private final ServidorInterface servidor;
    private int requestIdCounter = 0;

    public ConexaoRMI(String host, int porta) throws Exception {
        String url = "rmi://" + host + ":" + porta + "/BancoService";
        this.servidor = (ServidorInterface) Naming.lookup(url);
    }

    public byte[] doOperation(String objectReference, int methodId, byte[] arguments)
            throws RemoteException {
        int reqId = ++requestIdCounter;

        try {
            com.banco.protocolo.MensagemRMI mensagem =
                    com.banco.protocolo.MensagemRMI.newBuilder()
                            .setMessageType(0)
                            .setRequestId(reqId)
                            .setObjectReference(objectReference)
                            .setMethodId(String.valueOf(methodId))
                            .setArguments(
                                    com.google.protobuf.ByteString.copyFrom(arguments))
                            .build();

            return servidor.processarOperacao(mensagem.toByteArray());

        } catch (Exception e) {
            throw new RemoteException("Falha em doOperation: " + e.getMessage(), e);
        }
    }

    public void fechar() {
    }
}
