package models;

import interfaces.Tributavel;

import java.math.BigDecimal;

public class ContaCorrente extends Conta implements Tributavel {

    private double limite;
    private static int contadorNumero = 1000;

    public ContaCorrente(Cliente titular, String senha) {
        super(contadorNumero++, 0, titular, senha);
        this.limite = 1200;
    }

    public ContaCorrente(int numero, double saldo, Cliente titular, double limite, String senha) {
        super(numero, saldo, titular, senha);
        this.limite = limite;

        if (numero >= contadorNumero) {
            contadorNumero = numero + 1;
        }
    }

    public double getLimite() {
        return limite;
    }

    public void setLimite(double limite) {
        this.limite = limite;
    }

    @Override
    public double calcularImposto() {
        return 0.5;
    }

}
