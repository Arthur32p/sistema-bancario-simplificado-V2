package servidor;

import interfaces.ServidorInterface;
import models.Banco;
import interfaces.ClienteService;
import service.ClienteServiceImpl;
import interfaces.ContaService;
import service.ContaServiceImpl;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class ServidorRMI extends UnicastRemoteObject implements ServidorInterface {
    private final ContaService contaService;
    private final ClienteService clienteService;

    public ServidorRMI() throws RemoteException {
        super();
        Banco banco = new Banco();

        this.contaService = new ContaServiceImpl(banco);
        this.clienteService = new ClienteServiceImpl(banco);
    }

    @Override
    public byte[] processarOperacao(byte[] pacote) throws RemoteException {
        try{
            byte[] resposta = Dispatcher.processar(pacote, contaService, clienteService);
            sendReply(resposta);
            return resposta;
        } catch (IOException e){
            throw new RemoteException("Erro ao processar operação", e);
        }
    }

    @Override
    public void sendReply(byte[] reply) {
        System.out.println(reply.length + " bytes prontos para envio");
    }

    public static void main(String[] args) {
        try {

            System.setProperty("java.rmi.server.hostname", "10.10.255.63");

            LocateRegistry.createRegistry(1099);

            ServidorRMI servidor = new ServidorRMI();
            Naming.rebind("BancoService", servidor);

            System.out.println("Servidor RMI pronto!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}