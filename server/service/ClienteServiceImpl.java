package service;

import interfaces.ClienteService;
import models.Banco;
import models.Cliente;
import models.Conta;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ClienteServiceImpl extends UnicastRemoteObject implements ClienteService {

    private final Banco banco;

    public ClienteServiceImpl(Banco banco) throws RemoteException {
        super();
        this.banco = banco;
    }

    @Override
    public synchronized Cliente salvarOuObter(String nome, String cpf){
        Cliente existente = buscarPorCpf(cpf);
        if(existente != null){
            return existente;
        }

        Cliente novo = new Cliente(nome, cpf);
        this.banco.getClientes().add(novo);
        return novo;
    }

    @Override
    public synchronized Cliente buscarPorCpf(String cpf){
        for(Cliente c: banco.getClientes()){
            if(c.getCpf().equals(cpf)){
                return c;
            }
        }

        return null;
    }

    @Override
    public synchronized List<Conta> listarContas(String cpf){
        List<Conta> contas = new ArrayList<>();
        for(Conta c: banco.getContas()){
            if(c.getTitular().getCpf().equals(cpf)){
                contas.add(c);
            }
        }

        return contas;
    }
}
