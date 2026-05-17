package ui;


public class Banner {

    public static final int LARGURA   = 60;
    public static final String NOME   = "Banco RMI";
    public static final String VERSAO = "v2.0 Java · Protobuf · RMI";

    public static final String SEPARADOR      = "─".repeat(LARGURA);
    public static final String SEPARADOR_DUPLO = "═".repeat(LARGURA);

    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String CYAN    = "\u001B[36m";
    private static final String GREEN   = "\u001B[32m";
    private static final String RED     = "\u001B[31m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String GRAY    = "\u001B[90m";
    private static final String WHITE   = "\u001B[97m";
    private static final String B_CYAN  = BOLD + CYAN;
    private static final String B_GREEN = BOLD + GREEN;
    private static final String B_RED   = BOLD + RED;
    private static final String B_YELLOW= BOLD + YELLOW;
    private static final String B_WHITE = BOLD + WHITE;

    private static final String[] LOGO = {
        " ██████╗  █████╗ ███╗   ██╗ ██████╗ ██████╗ ",
        " ██╔══██╗██╔══██╗████╗  ██║██╔════╝██╔═══██╗",
        " ██████╔╝███████║██╔██╗ ██║██║     ██║   ██║",
        " ██╔══██╗██╔══██║██║╚██╗██║██║     ██║   ██║",
        " ██████╔╝██║  ██║██║ ╚████║╚██████╗╚██████╔╝",
        " ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚═════╝ "
    };



    public static String margem() {
        return "  ";
    }

    public static void limpar() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb = os.contains("win")
                    ? new ProcessBuilder("cmd", "/c", "cls")
                    : new ProcessBuilder("clear");
            pb.inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    public static void exibirCabecalho() {
        exibirCabecalho(null);
    }

    public static void exibirCabecalho(String subtitulo) {
        String m = margem();
        System.out.println();

        int meio = LOGO.length / 2;
        for (int i = 0; i < LOGO.length; i++) {
            String sufixo = (i == meio) ? "  " + bold(branco(NOME)) : "";
            System.out.println(m + ciano(LOGO[i]) + sufixo);
        }

        System.out.println(m + cinza(padEsquerda(VERSAO, LARGURA)));
        System.out.println(m + B_CYAN + SEPARADOR_DUPLO + RESET);

        if (subtitulo != null && !subtitulo.isBlank()) {
            System.out.println(m + amarelo(centralizar(subtitulo, LARGURA)));
        }
        System.out.println();
    }

    public static void tituloSecao(String texto) {
        String m = margem();
        int li = LARGURA - 2;
        System.out.println(m + amarelo("┌" + "─".repeat(li) + "┐"));
        System.out.println(m + amarelo("│") + bold(branco(centralizar(texto, li))) + amarelo("│"));
        System.out.println(m + amarelo("└" + "─".repeat(li) + "┘"));
        System.out.println();
    }

    public static void labelSecao(String texto) {
        String m = margem();
        System.out.println(m + B_CYAN + "▸ " + RESET + cinza(texto));
        System.out.println(m + cinza(SEPARADOR));
    }

    public static void campo(String label, String valor) {
        campo(label, valor, 16);
    }

    public static void campo(String label, String valor, int larguraLabel) {
        String m = margem();
        String rotulo = cinza(String.format("%-" + larguraLabel + "s", label));
        System.out.println(m + "  " + rotulo + valor);
    }

    public static void sucesso(String msg) {
        System.out.println();
        System.out.println(margem() + B_GREEN + "  ✔  " + RESET + verde(msg));
    }

    public static void erro(String msg) {
        System.out.println();
        System.out.println(margem() + B_RED + "  ✖  " + RESET + vermelho(msg));
    }

    public static void info(String msg) {
        System.out.println(margem() + B_CYAN + "  ℹ  " + RESET + ciano(msg));
    }

    public static void espaco() { System.out.println(); }

    public static void linhaSeparadora() {
        System.out.println(margem() + cinza(SEPARADOR));
    }

    public static void aguardeEnter(java.util.Scanner sc) {
        System.out.print(margem() + cinza("  Pressione ENTER para continuar..."));
        sc.nextLine();
    }


    public static String formatarDinheiro(double valor) {
        return String.format("R$ %.2f", valor);
    }

    public static String valorPositivo(double valor) {
        return verde(formatarDinheiro(valor));
    }

    public static String valorNegativo(double valor) {
        return vermelho(formatarDinheiro(valor));
    }

    public static String badge(String texto) {
        return B_CYAN + " " + texto + " " + RESET;
    }

    public static String badgeVerde(String texto) {
        return B_GREEN + " " + texto + " " + RESET;
    }

    public static String badgeAzul(String texto) {
        return "\u001B[1;34m" + " " + texto + " " + RESET;
    }


    public static String ciano(String s)    { return B_CYAN   + s + RESET; }
    public static String verde(String s)    { return B_GREEN  + s + RESET; }
    public static String vermelho(String s) { return B_RED    + s + RESET; }
    public static String amarelo(String s)  { return B_YELLOW + s + RESET; }
    public static String cinza(String s)    { return GRAY     + s + RESET; }
    public static String branco(String s)   { return B_WHITE  + s + RESET; }
    public static String bold(String s)     { return BOLD     + s + RESET; }


    public static String centralizar(String texto, int largura) {
        if (texto.length() >= largura) return texto;
        int pad = (largura - texto.length()) / 2;
        return " ".repeat(pad) + texto + " ".repeat(largura - texto.length() - pad);
    }

    public static String padEsquerda(String texto, int largura) {
        if (texto.length() >= largura) return texto;
        return " ".repeat(largura - texto.length()) + texto;
    }

    public static void prompt(String label) {
        System.out.print(margem() + "  " + cinza(label) + " ");
    }
}
