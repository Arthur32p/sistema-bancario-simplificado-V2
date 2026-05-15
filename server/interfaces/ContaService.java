package interfaces;

import models.Cliente;
import models.Conta;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ContaService extends Remote {
    boolean abrirConta(Cliente cliente, String senha, int tipo) throws RemoteException;
    boolean sacar(Conta conta, double valor) throws RemoteException;
    boolean depositar(Conta conta, double valor) throws RemoteException;
    boolean transferir(Conta origem, Conta destino, double valor) throws RemoteException;
    boolean pagar(Conta conta, double valor, String descricao) throws RemoteException;
    double projetarRendimento(Conta conta, int meses) throws RemoteException;
    Conta buscarConta(int numero) throws RemoteException;
    void registrar(int numeroConta, String mensagem) throws RemoteException;
    List<String> consultarExtrato(int numeroConta) throws RemoteException;
}