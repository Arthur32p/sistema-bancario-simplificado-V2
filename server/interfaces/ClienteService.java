package interfaces;

import models.Cliente;
import models.Conta;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClienteService extends Remote {
    Cliente salvarOuObter(String nome, String cpf)  throws RemoteException;
    Cliente buscarPorCpf(String cpf)  throws RemoteException;
    List<Conta> listarContas(String cpf)   throws RemoteException;
}
