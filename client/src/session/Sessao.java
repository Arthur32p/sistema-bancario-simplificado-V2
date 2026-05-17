package session;

public class Sessao {
    private final int numeroConta;
    private final String cpf;
    private final String nomeTitular;
    private final int tipoConta; // 1 = Corrente, 2 = Poupança
    private double saldo;
    private final double limite;
    private final double rendimento;

    public Sessao(int numeroConta, String cpf, String nomeTitular,
                  int tipoConta, double saldo, double limite, double rendimento) {
        this.numeroConta  = numeroConta;
        this.cpf          = cpf;
        this.nomeTitular  = nomeTitular;
        this.tipoConta    = tipoConta;
        this.saldo        = saldo;
        this.limite       = limite;
        this.rendimento   = rendimento;
    }

    public int    getNumeroConta()  { return numeroConta; }
    public String getCpf()         { return cpf; }
    public String getNomeTitular() { return nomeTitular; }
    public int    getTipoConta()   { return tipoConta; }
    public double getSaldo()       { return saldo; }
    public void   setSaldo(double saldo) { this.saldo = saldo; }
    public double getLimite()      { return limite; }
    public double getRendimento()  { return rendimento; }

    public boolean isCorrente() { return tipoConta == 1; }
    public boolean isPoupanca() { return tipoConta == 2; }

    public String tipoConta() { return tipoConta == 1 ? "Corrente" : "Poupança"; }
}
