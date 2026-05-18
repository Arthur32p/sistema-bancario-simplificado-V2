package ui;

import rmi.ConexaoRMI;
import rmi.ServicoCliente;
import session.Sessao;

import java.util.Scanner;

/**
 * Tela de login — inspirada no tela_login.rb.
 * Coleta CPF, senha e tipo de conta, invoca o servidor via RMI.
 */
public class TelaLogin {

    private final Scanner sc;
    private final ServicoCliente servicoCliente;

    public TelaLogin(Scanner sc, ConexaoRMI conexao) {
        this.sc             = sc;
        this.servicoCliente = new ServicoCliente(conexao);
    }

    /**
     * Exibe a tela de login e retorna uma Sessao em caso de sucesso,
     * ou null se o usuário cancelar / houver erro.
     */
    public Sessao exibir() {
        Banner.limpar();
        Banner.exibirCabecalho("ACESSO À CONTA");
        Banner.tituloSecao("IDENTIFICAÇÃO DO CLIENTE");

        // CPF
        String cpf = lerCampo("CPF          : ");
        if (cpf == null) return null;

        // Senha
        String senha = lerSenha("Senha        : ");
        if (senha == null) return null;

        // Tipo de conta
        Banner.espaco();
        Banner.labelSecao("TIPO DE CONTA");
        Banner.espaco();
        System.out.println(Banner.margem() + "  " + Banner.cinza("[1]  Corrente"));
        System.out.println(Banner.margem() + "  " + Banner.cinza("[2]  Poupança"));
        System.out.println(Banner.margem() + "  " + Banner.cinza("[0]  Voltar"));
        Banner.espaco();
        Banner.prompt("Tipo         :");
        String tipoStr = sc.nextLine().trim();

        if (tipoStr.equals("0")) return null;
        int tipo;
        try {
            tipo = Integer.parseInt(tipoStr);
            if (tipo < 1 || tipo > 2) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Banner.erro("Tipo inválido.");
            pausa(1500);
            return null;
        }

        Banner.espaco();
        Banner.linhaSeparadora();
        Banner.espaco();
        Banner.info("Autenticando...");

        ServicoCliente.ResultadoLogin resultado = servicoCliente.login(cpf, senha, tipo);

        if (resultado.sucesso()) {
            Banner.sucesso(resultado.mensagem());
            pausa(1000);
            return resultado.sessao();
        } else {
            Banner.erro(resultado.mensagem());
            pausa(2000);
            return null;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private String lerCampo(String label) {
        Banner.prompt(label);
        String valor = sc.nextLine().trim();
        if (valor.isBlank()) {
            Banner.erro("Campo obrigatório.");
            pausa(1000);
            return null;
        }
        return valor;
    }

    private String lerSenha(String label) {
        // Em Java puro no terminal, lemos normalmente (sem mascaramento nativo fácil)
        // Console.readPassword() funciona apenas quando não há redirecionamento
        Banner.prompt(label);
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword();
            if (chars == null || chars.length == 0) {
                Banner.erro("Senha obrigatória.");
                pausa(1000);
                return null;
            }
            return new String(chars);
        } else {
            // Fallback quando Console não está disponível (IDE, pipe)
            String valor = sc.nextLine().trim();
            if (valor.isBlank()) {
                Banner.erro("Senha obrigatória.");
                pausa(1000);
                return null;
            }
            return valor;
        }
    }

    private void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
