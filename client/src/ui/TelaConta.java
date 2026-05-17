package ui;

import rmi.ConexaoRMI;
import rmi.ServicoConta;
import session.Sessao;

import java.util.Scanner;

public class TelaConta {

    private final Scanner sc;
    private final ServicoConta servicoConta;
    private final Sessao sessao;

    public TelaConta(Scanner sc, ConexaoRMI conexao, Sessao sessao) {
        this.sc          = sc;
        this.servicoConta = new ServicoConta(conexao);
        this.sessao       = sessao;
    }

    public void exibir() {
        boolean logado = true;

        while (logado) {
            renderizarDashboard();
            exibirMenu();

            String opcao = sc.nextLine().trim();

            switch (opcao) {
                case "1" -> operacaoDepositar();
                case "2" -> operacaoSacar();
                case "3" -> operacaoTransferir();
                case "4" -> operacaoExtrato();
                case "5" -> {
                    if (sessao.isCorrente()) operacaoPagar();
                    else if (sessao.isPoupanca()) operacaoProjetarRendimento();
                    else opcaoInvalida();
                }
                case "6" -> {
                    if (sessao.isCorrente()) operacaoProjetarRendimento();
                    else opcaoInvalida();
                }
                case "0" -> {
                    Banner.limpar();
                    Banner.exibirCabecalho();
                    Banner.info("Sessão encerrada. Até logo!");
                    pausa(1500);
                    logado = false;
                }
                default -> opcaoInvalida();
            }
        }
    }


    private void renderizarDashboard() {
        Banner.limpar();
        Banner.exibirCabecalho("MINHA CONTA");
        Banner.tituloSecao("PAINEL DO CLIENTE");

        Banner.labelSecao("TITULAR");
        Banner.campo("Nome",     Banner.branco(sessao.getNomeTitular()));
        Banner.campo("CPF",      Banner.cinza(sessao.getCpf()));
        Banner.espaco();

        Banner.labelSecao("CONTA");
        Banner.campo("Número",   Banner.branco(String.valueOf(sessao.getNumeroConta())));
        Banner.campo("Tipo",     sessao.isCorrente()
                ? Banner.badgeAzul("CORRENTE")
                : Banner.badgeVerde("POUPANÇA"));
        Banner.espaco();

        Banner.labelSecao("SALDO DISPONÍVEL");
        System.out.println(Banner.margem() + "  " + Banner.valorPositivo(sessao.getSaldo()));
        if (sessao.isCorrente()) {
            System.out.println(Banner.margem() + "  " + Banner.cinza(String.format("Limite: R$ %.2f", sessao.getLimite())));
        } else {
            System.out.println(Banner.margem() + "  " + Banner.cinza(String.format("Rendimento: %.3f%% a.m.", sessao.getRendimento() * 100)));
        }
        Banner.espaco();
        Banner.linhaSeparadora();
        Banner.espaco();
    }

    private void exibirMenu() {
        String m = Banner.margem();
        System.out.println(m + "  " + Banner.cinza("[1]  Depositar"));
        System.out.println(m + "  " + Banner.cinza("[2]  Sacar"));
        System.out.println(m + "  " + Banner.cinza("[3]  Transferir"));
        System.out.println(m + "  " + Banner.cinza("[4]  Ver Extrato"));

        if (sessao.isCorrente()) {
            System.out.println(m + "  " + Banner.cinza("[5]  Pagar Boleto / Conta"));
            System.out.println(m + "  " + Banner.cinza("[6]  Projetar Rendimento  " +
                    Banner.cinza("(apenas Poupança)")));
        } else {
            System.out.println(m + "  " + Banner.cinza("[5]  Projetar Rendimento"));
        }

        System.out.println(m + "  " + Banner.cinza("[0]  Sair (Logout)"));
        Banner.espaco();
        Banner.prompt("O que deseja fazer?");
    }

    // ── Operações bancárias ──────────────────────────────────────────

    private void operacaoDepositar() {
        Double valor = lerValor("Valor do depósito");
        if (valor == null) return;

        Banner.info("Processando...");
        ServicoConta.ResultadoSimples res = servicoConta.depositar(sessao.getNumeroConta(), valor);

        if (res.ok()) {
            sessao.setSaldo(sessao.getSaldo() + valor);
            Banner.sucesso(res.mensagem());
        } else {
            Banner.erro(res.mensagem());
        }
        pausa(2000);
    }

    private void operacaoSacar() {
        Double valor = lerValor("Valor do saque");
        if (valor == null) return;

        Banner.info("Processando...");
        ServicoConta.ResultadoSimples res = servicoConta.sacar(sessao.getNumeroConta(), valor);

        if (res.ok()) {
            sessao.setSaldo(sessao.getSaldo() - valor);
            Banner.sucesso(res.mensagem());
        } else {
            Banner.erro(res.mensagem());
        }
        pausa(2000);
    }

    private void operacaoTransferir() {
        Banner.espaco();
        Banner.labelSecao("TRANSFERÊNCIA");

        Banner.prompt("Conta destino      :");
        String destStr = sc.nextLine().trim();
        int numDestino;
        try {
            numDestino = Integer.parseInt(destStr);
        } catch (NumberFormatException e) {
            Banner.erro("Número de conta inválido.");
            pausa(1500);
            return;
        }

        if (numDestino == sessao.getNumeroConta()) {
            Banner.erro("Não é possível transferir para a própria conta.");
            pausa(2000);
            return;
        }

        Double valor = lerValor("Valor da transferência");
        if (valor == null) return;

        Banner.info("Processando...");
        ServicoConta.ResultadoTransferencia res =
                servicoConta.transferir(sessao.getNumeroConta(), numDestino, valor);

        if (res.ok()) {
            sessao.setSaldo(sessao.getSaldo() - valor);
            Banner.sucesso(res.mensagem() + " → " + Banner.branco(res.nomeDestino()));
        } else {
            Banner.erro(res.mensagem());
        }
        pausa(2500);
    }

    private void operacaoPagar() {
        Banner.espaco();
        Banner.labelSecao("PAGAMENTO DE BOLETO / CONTA");

        Banner.prompt("Descrição          :");
        String descricao = sc.nextLine().trim();
        if (descricao.isBlank()) {
            Banner.erro("Descrição obrigatória.");
            pausa(1500);
            return;
        }

        Double valor = lerValor("Valor do pagamento");
        if (valor == null) return;

        Banner.info("Processando...");
        ServicoConta.ResultadoSimples res =
                servicoConta.pagar(sessao.getNumeroConta(), valor, descricao);

        if (res.ok()) {
            sessao.setSaldo(sessao.getSaldo() - valor);
            Banner.sucesso(res.mensagem() + " — " + descricao);
        } else {
            Banner.erro(res.mensagem());
        }
        pausa(2000);
    }

    private void operacaoProjetarRendimento() {
        if (!sessao.isPoupanca()) {
            Banner.erro("Projeção de rendimento disponível apenas para Conta Poupança.");
            pausa(2000);
            return;
        }

        Banner.espaco();
        Banner.labelSecao("PROJEÇÃO DE RENDIMENTO");
        Banner.prompt("Número de meses    :");
        String mesesStr = sc.nextLine().trim();
        int meses;
        try {
            meses = Integer.parseInt(mesesStr);
            if (meses <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Banner.erro("Número de meses inválido.");
            pausa(1500);
            return;
        }

        Banner.info("Calculando...");
        ServicoConta.ResultadoRendimento res =
                servicoConta.projetarRendimento(sessao.getNumeroConta(), meses);

        if (res.ok()) {
            Banner.espaco();
            Banner.labelSecao("RESULTADO DA PROJEÇÃO");
            Banner.campo("Período",         meses + " meses");
            Banner.campo("Saldo atual",     Banner.formatarDinheiro(sessao.getSaldo()));
            Banner.campo("Saldo projetado", Banner.valorPositivo(res.valor()));
            Banner.espaco();
            Banner.aguardeEnter(sc);
        } else {
            Banner.erro(res.mensagem());
            pausa(2000);
        }
    }

    private void operacaoExtrato() {
        Banner.limpar();
        Banner.exibirCabecalho("EXTRATO");
        Banner.tituloSecao("MOVIMENTAÇÕES DA CONTA " + sessao.getNumeroConta());

        Banner.info("Buscando movimentações...");
        ServicoConta.ResultadoExtrato res = servicoConta.extrato(sessao.getNumeroConta());

        if (res.ok()) {
            Banner.espaco();
            if (res.linhas().isEmpty()) {
                Banner.info("Nenhuma movimentação registrada.");
            } else {
                Banner.labelSecao("HISTÓRICO");
                for (String linha : res.linhas()) {
                    System.out.println(Banner.margem() + "  " + Banner.cinza("• ") + linha);
                }
            }
        } else {
            Banner.erro(res.mensagem());
        }

        Banner.espaco();
        Banner.linhaSeparadora();
        Banner.espaco();
        Banner.aguardeEnter(sc);
    }


    private Double lerValor(String label) {
        Banner.espaco();
        Banner.prompt(label + "  : R$ ");
        String s = sc.nextLine().trim().replace(",", ".");
        try {
            double v = Double.parseDouble(s);
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            Banner.erro("Valor inválido. Informe um número positivo.");
            pausa(1500);
            return null;
        }
    }

    private void opcaoInvalida() {
        Banner.erro("Opção inválida.");
        pausa(1000);
    }

    private void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
