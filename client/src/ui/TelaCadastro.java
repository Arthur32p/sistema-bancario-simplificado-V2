package ui;

import rmi.ConexaoRMI;
import rmi.ServicoCliente;

import java.util.Scanner;

/**
 * Tela de cadastro — inspirada em tela_cadastro.rb.
 * Coleta dados do cliente e abre conta via RMI.
 */
public class TelaCadastro {

    private static final String[][] TIPOS = {
            {"1", "Corrente",  "Limite R$ 1.200,00",     "CORRENTE"},
            {"2", "Poupança",  "Rendimento 0,500% a.m.", "POUPANÇA"},
    };

    private final Scanner sc;
    private final ServicoCliente servicoCliente;

    public TelaCadastro(Scanner sc, ConexaoRMI conexao) {
        this.sc             = sc;
        this.servicoCliente = new ServicoCliente(conexao);
    }

    public void exibir() {
        Banner.limpar();
        Banner.exibirCabecalho("ABERTURA DE CONTA");
        Banner.tituloSecao("DADOS PESSOAIS");

        // Coleta dados
        String nome = lerObrigatorio("Nome completo  : ");
        if (nome == null) return;

        String cpf = lerObrigatorio("CPF            : ");
        if (cpf == null) return;

        String senha = lerSenha("Senha          : ");
        if (senha == null) return;

        String confirmaSenha = lerSenha("Confirme senha : ");
        if (confirmaSenha == null) return;

        if (!senha.equals(confirmaSenha)) {
            Banner.erro("As senhas não coincidem.");
            pausa(2000);
            return;
        }

        // Tipo de conta
        Banner.espaco();
        Banner.labelSecao("TIPO DE CONTA");
        Banner.espaco();
        for (String[] tipo : TIPOS) {
            String detalhe = Banner.cinza("(" + tipo[2] + ")");
            System.out.println(Banner.margem() + "  " +
                    Banner.cinza("[" + tipo[0] + "]  ") +
                    Banner.branco(String.format("%-12s", tipo[1])) + "  " + detalhe);
        }
        System.out.println(Banner.margem() + "  " + Banner.cinza("[0]  Voltar"));
        Banner.espaco();
        Banner.prompt("Tipo           :");
        String tipoStr = sc.nextLine().trim();

        if (tipoStr.equals("0")) return;
        int tipo;
        try {
            tipo = Integer.parseInt(tipoStr);
            if (tipo < 1 || tipo > 2) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Banner.erro("Tipo inválido.");
            pausa(1500);
            return;
        }

        // Resumo
        Banner.espaco();
        Banner.linhaSeparadora();
        Banner.espaco();
        exibirResumo(nome, cpf, tipo);
        Banner.espaco();
        Banner.linhaSeparadora();
        Banner.espaco();

        // Confirmação
        System.out.println(Banner.margem() + "  " + Banner.cinza("[1]  Confirmar abertura"));
        System.out.println(Banner.margem() + "  " + Banner.cinza("[0]  Cancelar"));
        Banner.espaco();
        Banner.prompt("Escolha        :");
        String conf = sc.nextLine().trim();

        if (!conf.equals("1")) {
            Banner.info("Operação cancelada.");
            pausa(1200);
            return;
        }

        Banner.espaco();
        Banner.info("Cadastrando...");

        ServicoCliente.ResultadoCadastro resultado = servicoCliente.cadastrar(nome, cpf, senha, tipo);

        if (resultado.sucesso()) {
            Banner.sucesso(resultado.mensagem());
            Banner.info("Você já pode fazer login com seu CPF e senha.");
        } else {
            Banner.erro(resultado.mensagem());
        }

        pausa(2500);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private void exibirResumo(String nome, String cpf, int tipo) {
        String[] info = TIPOS[tipo - 1];
        Banner.labelSecao("RESUMO DA ABERTURA");
        Banner.campo("Titular",    Banner.branco(nome));
        Banner.campo("CPF",        Banner.cinza(cpf));
        Banner.campo("Tipo",       tipo == 1 ? Banner.badgeAzul(info[3]) : Banner.badgeVerde(info[3]));
        Banner.campo("Condições",  Banner.cinza(info[2]));
    }

    private String lerObrigatorio(String label) {
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
