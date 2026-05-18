package src;

import rmi.ConexaoRMI;
import session.Sessao;
import ui.Banner;
import ui.TelaLogin;
import ui.TelaCadastro;
import ui.TelaConta;

/**
 * Ponto de entrada do cliente bancário RMI.
 * Gerencia o fluxo principal: menu inicial → login/cadastro → conta.
 */
public class ClienteApp {

    public static void main(String[] args) {
        // Endereço do servidor (pode ser passado como argumento)
        String host = (args.length > 0) ? args[0] : "localhost";
        int porta   = (args.length > 1) ? Integer.parseInt(args[1]) : 1099;

        Banner.limpar();
        Banner.exibirCabecalho();

        // Tenta conectar ao servidor
        ConexaoRMI conexao;
        try {
            conexao = new ConexaoRMI(host, porta);
        } catch (Exception e) {
            Banner.erro("Não foi possível conectar ao servidor: " + e.getMessage());
            Banner.info("Verifique se o servidor está rodando em " + host + ":" + porta);
            System.exit(1);
            return;
        }

        Banner.sucesso("Conectado ao servidor " + host + ":" + porta);
        pausa(1000);

        // Loop do menu principal
        java.util.Scanner sc = new java.util.Scanner(System.in);
        boolean rodando = true;

        while (rodando) {
            Banner.limpar();
            Banner.exibirCabecalho("MENU PRINCIPAL");
            exibirMenuPrincipal();

            String opcao = sc.nextLine().trim();
            switch (opcao) {
                case "1" -> {
                    TelaLogin telaLogin = new TelaLogin(sc, conexao);
                    Sessao sessao = telaLogin.exibir();
                    if (sessao != null) {
                        TelaConta telaConta = new TelaConta(sc, conexao, sessao);
                        telaConta.exibir();
                    }
                }
                case "2" -> {
                    TelaCadastro telaCadastro = new TelaCadastro(sc, conexao);
                    telaCadastro.exibir();
                }
                case "0" -> {
                    Banner.limpar();
                    Banner.exibirCabecalho();
                    Banner.info("Obrigado por usar o Banco RMI. Até logo!");
                    pausa(1500);
                    rodando = false;
                }
                default -> {
                    Banner.erro("Opção inválida. Tente novamente.");
                    pausa(1000);
                }
            }
        }

        conexao.fechar();
        sc.close();
    }

    private static void exibirMenuPrincipal() {
        String m = Banner.margem();
        System.out.println(m + Banner.SEPARADOR);
        System.out.println(m + "  [1]  Entrar na minha conta");
        System.out.println(m + "  [2]  Abrir nova conta");
        System.out.println(m + "  [0]  Sair");
        System.out.println(m + Banner.SEPARADOR);
        System.out.print(m + "  Escolha: ");
    }

    private static void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
