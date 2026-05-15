package service;

import interfaces.ContaService;
import models.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContaServiceImpl extends UnicastRemoteObject implements ContaService {

    private final Banco banco;
    private final Map<Integer, List<String>> repositorioExtratos = new HashMap<>();

    public ContaServiceImpl(Banco banco) throws RemoteException {
        super();
        this.banco = banco;
    }

    @Override
    public synchronized boolean abrirConta(Cliente cliente, String senha, int tipo){

        for (Conta c : banco.getContas()) {
            if (c.getTitular().getCpf().equals(cliente.getCpf())) {
                if ((tipo == 1 && c instanceof ContaCorrente) ||
                        (tipo == 2 && c instanceof ContaPoupanca)) {
                    throw new IllegalArgumentException("Tipo de conta já existente para este CPF.");
                }
            }
        }

        Conta nova = (tipo == 1) ? new ContaCorrente(cliente, senha) : new ContaPoupanca(cliente, senha);
        banco.getContas().add(nova);

        registrar(nova.getNumero(), "Conta aberta em 2026-04-13.");

        return true;
    }

    @Override
    public synchronized boolean sacar(Conta conta, double valor){
        if(valor >= 0 && conta.getSaldo() >= valor){
            conta.setSaldo(conta.getSaldo() - valor);
            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean depositar(Conta conta, double valor){
        if(valor > 0){
            conta.setSaldo(conta.getSaldo() + valor);
            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean transferir(Conta origem, Conta destino, double valor){
        double imposto = 0;
        double limite = 0;

        if (origem instanceof ContaCorrente cc) {
            imposto = cc.calcularImposto();
            limite = cc.getLimite();
        }

        double totalADebitar = valor + imposto;

        if(valor <= limite && sacar(origem, totalADebitar) ){
            depositar(destino, valor);

            registrar(origem.getNumero(), "Transferência enviada: -R$ " + valor + " (Imposto: R$ " + imposto + ")");
            registrar(destino.getNumero(), "Transferência recebida: +R$ " + valor + " de " + origem.getTitular().getNome());

            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean pagar(Conta conta, double valor, String descricao){
        if(conta instanceof ContaCorrente cc){
            if (sacar(cc, valor)){
                registrar(conta.getNumero(), "Pagamento: " + descricao + " | Valor: -R$ " + valor);

                return true;
            }
        } else {
            return false;
        }

        return false;
    }

    @Override
    public synchronized double projetarRendimento(Conta conta, int meses){
        if(!(conta instanceof ContaPoupanca)){
            return -1;
        }

        ContaPoupanca cp = (ContaPoupanca) conta;
        double taxa = cp.getRendimento();
        double saldo = cp.getSaldo();

        return saldo * Math.pow((1 + taxa), meses);
    }

    @Override
    public synchronized Conta buscarConta(int numero){
        for(Conta c: banco.getContas()){
            if(c.getNumero() == numero){
                return c;
            }
        }

        return null;
    }

    @Override
    public void registrar(int numeroConta, String mensagem) {
        repositorioExtratos
                .computeIfAbsent(numeroConta, k -> new ArrayList<>())
                .add(mensagem);
    }

    @Override
    public List<String> consultarExtrato(int numeroConta) {
        return repositorioExtratos.getOrDefault(numeroConta, new ArrayList<>());
    }
}