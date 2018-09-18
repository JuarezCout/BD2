import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class Leitor {
    public static List<Container> containers = new ArrayList<Container>();

    void criaContainers(){
        String arquivo1 = "C:\\Users\\Rodrigo\\Desktop\\forn-tpch.txt";
        String arquivo2 = "C:\\Users\\Rodrigo\\Desktop\\cli-tpch.txt";
        //String arquivo1 = "C:\\Users\\Juarez Coutinho\\Desktop\\forn-tpch.txt";
        //String arquivo2 = "C:\\Users\\Juarez Coutinho\\Desktop\\cli-tpch.txt";

        iniciarLeitura(arquivo1);
        //iniciarLeitura(arquivo2);

        System.out.println("Containers criados");

        System.out.println("/n/n/n");
        System.out.println("TESTE BUFFER/n/n/n");
        GerenciadorBuffer.geraRequisicoes();
        //Gravador.exportaArquivoTxt(containers);

    }

    void iniciarLeitura(String arquivoCaminho){
        System.out.println("Iniciando leitura do arquivo...");

        try {
            RandomAccessFile arquivo = new RandomAccessFile(arquivoCaminho, "rw");

            String linha = arquivo.readLine();

            Container container = new Container(linha);
            System.out.println("Gerado Bloco de Controle");

           while((linha = arquivo.readLine()) != null) {
                adicionarTupla(Tupla.montaTuplaByte(separador(linha)), container);
           }

           arquivo.close();
           containers.add(container);

        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    void adicionarTupla(byte[] tupla, Container container) {
        int idBlocoLivre = Bloco.byteToInt(Bloco.getBytes(container.controle.dados, 5, 3));
        //se nao exitir bloco, deve ser criado
        if(idBlocoLivre == 0) {
            Bloco novo = new Bloco(1, container.getContainerId());
            System.out.println("Gerado bloco de ID: " + 1);
            novo.adicionarTuplaNoBloco(tupla);
            container.blocos.add(novo);
            container.atualizaIdLivreControle(1);
        } else { //bloco maior que tamanho da tupla
            if(container.tamanhoDoBloco() - container.encontrarBlocoPorId(idBlocoLivre).getTamanhoBloco() > tupla.length) {
                System.out.println("Salvou tupla no bloco: " + idBlocoLivre);
               // System.out.println("idmaiorq"+idBlocoLivre+"tamanho"+container.encontrarBlocoPorId(idBlocoLivre).getTamanhoBloco());
                container.encontrarBlocoPorId(idBlocoLivre).adicionarTuplaNoBloco(tupla);
            } else { //bloco menor que tamanho da tupla
               // System.out.println("idmenorq"+idBlocoLivre+"tamanho"+container.encontrarBlocoPorId(idBlocoLivre).getTamanhoBloco());
                Bloco novo = new Bloco(idBlocoLivre + 1, container.getContainerId());
                System.out.println("Gerado bloco de ID: " + (idBlocoLivre + 1));
                novo.adicionarTuplaNoBloco(tupla);
                container.blocos.add(novo);
                container.atualizaIdLivreControle(idBlocoLivre + 1);
            }
        }
    }

    String[] separador(String linha){
        return linha.split("\\|");
    }
}