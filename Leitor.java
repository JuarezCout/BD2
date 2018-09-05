import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class Leitor {
    ByteBuffer buf = ByteBuffer.allocate(3);
    Container container = new Container();

    void iniciarLeitura(){
        System.out.println("Iniciando leitura do arquivo...");

        try {
            RandomAccessFile arquivo = new RandomAccessFile("C:\\Users\\Rodrigo\\Desktop\\forn-tpch.txt", "rw");

            String linha = arquivo.readLine();

            container.setControle(new Bloco(linha));

           while((linha = arquivo.readLine()) != null) {
                adicionarTupla(Tupla.montaTuplaByte(separador(linha)));
           }

            arquivo.close();

           Gravador.salvaArquivo(container);

        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    void adicionarTupla(byte[] tupla) {
        int idBlocoLivre = Bloco.byteToInt(Bloco.getBytes(container.controle.dados, 5, 3));
        System.out.println(idBlocoLivre);
        //se nao exitir bloco, deve ser criado
        if(idBlocoLivre == 0) {
            Bloco novo = new Bloco(1);
            novo.adicionarTuplaNoBloco(tupla);
            container.blocos.add(novo);
            container.atualizaIdLivreControle(1);
        } else { //bloco maior que tamanho da tupla
            if(container.tamanhoDoBloco() - container.encontrarBlocoPorId(idBlocoLivre).getTamanhoBloco() > tupla.length) {
                System.out.println("idmaiorq"+idBlocoLivre+"tamanho"+container.encontrarBlocoPorId(idBlocoLivre).getTamanhoBloco());
                container.encontrarBlocoPorId(idBlocoLivre).adicionarTuplaNoBloco(tupla);
            } else { //bloco menor que tamanho da tupla
                System.out.println("idmenorq"+idBlocoLivre+"tamanho"+container.encontrarBlocoPorId(idBlocoLivre).getTamanhoBloco());
                Bloco novo = new Bloco(idBlocoLivre + 1);
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