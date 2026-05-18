package ui;

public class Banner {

    public static final int LARGURA = 60;

    public static final String NOME = "Banco RMI";
    public static final String VERSAO = "v2.0 Java · Protobuf · RMI";

    public static final String SEPARADOR = "─".repeat(LARGURA);
    public static final String SEPARADOR_DUPLO = "· ".repeat(LARGURA / 2);

    // ── ANSI RESET ──────────────────────────────────────────────────
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";

    // ── Paleta para terminal claro ─────────────────────────────────
    private static final String CINZA    = "\u001B[38;5;245m";
    private static final String AZUL     = "\u001B[38;5;33m";
    private static final String VERDE    = "\u001B[38;5;35m";
    private static final String VERMELHO = "\u001B[38;5;160m";
    private static final String AMARELO  = "\u001B[38;5;136m";
    private static final String CIANO    = "\u001B[38;5;37m";

    private static final String B_AZUL   = BOLD + AZUL;
    private static final String B_VERDE  = BOLD + VERDE;
    private static final String B_VERM   = BOLD + VERMELHO;
    private static final String B_AMAR   = BOLD + AMARELO;
    private static final String B_CIANO  = BOLD + CIANO;
    private static final String B_CINZA  = BOLD + CINZA;

    // ── Logo ────────────────────────────────────────────────────────
    private static final String[] LOGO = {
            " ██████╗  █████╗ ███╗   ██╗ ██████╗ ██████╗ ",
            " ██╔══██╗██╔══██╗████╗  ██║██╔════╝██╔═══██╗",
            " ██████╔╝███████║██╔██╗ ██║██║     ██║   ██║",
            " ██╔══██╗██╔══██║██║╚██╗██║██║     ██║   ██║",
            " ██████╔╝██║  ██║██║ ╚████║╚██████╗╚██████╔╝",
            " ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚═════╝ "
    };

    // ── Layout ─────────────────────────────────────────────────────

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

            String sufixo = (i == meio)
                    ? "   " + B_CINZA + NOME + RESET
                    : "";

            System.out.println(
                    m + B_AZUL + LOGO[i] + RESET + sufixo
            );
        }

        System.out.println(
                m + CINZA + padEsquerda(VERSAO, LARGURA) + RESET
        );

        System.out.println(
                m + CINZA + SEPARADOR_DUPLO + RESET
        );

        if (subtitulo != null && !subtitulo.isBlank()) {
            System.out.println(
                    m + B_AMAR + centralizar(subtitulo, LARGURA) + RESET
            );
        }

        System.out.println();
    }

    public static void tituloSecao(String texto) {

        String m = margem();
        int li = LARGURA - 2;

        System.out.println(
                m + CINZA + "┌" + "─".repeat(li) + "┐" + RESET
        );

        System.out.println(
                m + CINZA + "│"
                        + RESET
                        + B_CINZA
                        + centralizar(texto, li)
                        + RESET
                        + CINZA
                        + "│"
                        + RESET
        );

        System.out.println(
                m + CINZA + "└" + "─".repeat(li) + "┘" + RESET
        );

        System.out.println();
    }

    public static void labelSecao(String texto) {

        String m = margem();

        System.out.println(
                m + B_AZUL + "▸ " + texto + RESET
        );

        System.out.println(
                m + CINZA + SEPARADOR + RESET
        );
    }

    public static void campo(String label, String valor) {
        campo(label, valor, 16);
    }

    public static void campo(String label, String valor, int larguraLabel) {

        String m = margem();

        String rotulo = CINZA
                + String.format("%-" + larguraLabel + "s", label)
                + RESET;

        System.out.println(
                m + "  " + rotulo + valor
        );
    }

    // ── Feedback ────────────────────────────────────────────────────

    public static void sucesso(String msg) {

        System.out.println();

        System.out.println(
                margem()
                        + B_VERDE
                        + "  ✔  "
                        + msg
                        + RESET
        );
    }

    public static void erro(String msg) {

        System.out.println();

        System.out.println(
                margem()
                        + B_VERM
                        + "  ✖  "
                        + msg
                        + RESET
        );
    }

    public static void info(String msg) {

        System.out.println(
                margem()
                        + B_CIANO
                        + "  ℹ  "
                        + msg
                        + RESET
        );
    }

    // ── Espaçamento ─────────────────────────────────────────────────

    public static void espaco() {
        System.out.println();
    }

    public static void linhaSeparadora() {

        System.out.println(
                margem()
                        + CINZA
                        + SEPARADOR
                        + RESET
        );
    }

    public static void aguardeEnter(java.util.Scanner sc) {

        System.out.print(
                margem()
                        + CINZA
                        + "  Pressione ENTER para continuar..."
                        + RESET
        );

        sc.nextLine();
    }

    // ── Formatação monetária ────────────────────────────────────────

    public static String formatarDinheiro(double valor) {
        return String.format("R$ %.2f", valor);
    }

    public static String valorPositivo(double valor) {
        return B_VERDE + formatarDinheiro(valor) + RESET;
    }

    public static String valorNegativo(double valor) {
        return B_VERM + formatarDinheiro(valor) + RESET;
    }

    // ── Badges ──────────────────────────────────────────────────────

    public static String badge(String texto) {
        return B_CIANO + " " + texto + " " + RESET;
    }

    public static String badgeVerde(String texto) {
        return B_VERDE + " " + texto + " " + RESET;
    }

    public static String badgeAzul(String texto) {
        return B_AZUL + " " + texto + " " + RESET;
    }

    // ── Helpers de cor ──────────────────────────────────────────────

    public static String ciano(String s) {
        return B_CIANO + s + RESET;
    }

    public static String verde(String s) {
        return B_VERDE + s + RESET;
    }

    public static String vermelho(String s) {
        return B_VERM + s + RESET;
    }

    public static String amarelo(String s) {
        return B_AMAR + s + RESET;
    }

    public static String cinza(String s) {
        return CINZA + s + RESET;
    }

    public static String branco(String s) {
        return B_CINZA + s + RESET;
    }

    public static String bold(String s) {
        return BOLD + s + RESET;
    }

    // ── Helpers de layout ───────────────────────────────────────────

    public static String centralizar(String texto, int largura) {

        if (texto.length() >= largura) {
            return texto;
        }

        int pad = (largura - texto.length()) / 2;

        return " ".repeat(pad)
                + texto
                + " ".repeat(largura - texto.length() - pad);
    }

    public static String padEsquerda(String texto, int largura) {

        if (texto.length() >= largura) {
            return texto;
        }

        return " ".repeat(largura - texto.length()) + texto;
    }

    // ── Prompt ──────────────────────────────────────────────────────

    public static void prompt(String label) {

        System.out.print(
                margem()
                        + "  "
                        + B_AZUL
                        + label
                        + RESET
                        + " "
        );
    }
}