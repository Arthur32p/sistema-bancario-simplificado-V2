package models;

import java.math.BigDecimal;

public class ContaPoupanca extends Conta{

    private double rendimento;
    private static int contadorNumero = 5000;

    public ContaPoupanca(Cliente titular, String senha) {
        super(contadorNumero++, 0, titular, senha);
        this.rendimento = 0.005;
    }

    public ContaPoupanca(int numero, double saldo, Cliente titular, double rendimento, String senha) {
        super(numero, saldo, titular, senha);
        this.rendimento = rendimento;

        if(numero >= contadorNumero){
            contadorNumero = numero + 1;
        }
    }

    public double getRendimento() {
        return rendimento;
    }

    public void setRendimento(double rendimento) {
        this.rendimento = rendimento;
    }
}
