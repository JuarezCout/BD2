

public class App {
    public static void main(String[] args) {
        Leitor leitor = new Leitor();
        leitor.iniciarLeitura();
        Buffer.geraRequisicoes(leitor.container);
    }
}
