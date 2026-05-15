package models;

import java.math.BigDecimal;

public abstract class Conta {

    private int numero;
    private double saldo;
    private Cliente titular;
    private String senha;

    public Conta(int numero, double saldo, Cliente titular, String senha) {
        this.numero = numero;
        this.saldo = saldo;
        this.titular = titular;
        this.senha = senha;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public Cliente getTitular() {
        return titular;
    }

    public void setTitular(Cliente titular) {
        this.titular = titular;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
