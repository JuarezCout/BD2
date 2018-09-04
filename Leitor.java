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
            System.out.println("teste " + Bloco.byteToInt(Bloco.getBytes(container.controle.dados, 5, 3)));

           while((linha = arquivo.readLine()) != null) {
                adicionarTupla(Tupla.montaTuplaByte(separador(linha)));
           }


            arquivo.close();

        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    void adicionarTupla(byte[] tupla) {
        int idBlocoLivre = Bloco.byteToInt(Bloco.getBytes(container.controle.dados, 5, 3));

        System.out.println(container.encontrarBlocoPorId(idBlocoLivre));

        if(idBlocoLivre == 0) {
            Bloco novo = new Bloco(1);
            novo.adicionarTuplaNoBloco(tupla);
            container.blocos.add(novo);
            System.out.println(tupla.length);
            System.out.println(Bloco.byteToInt(Bloco.getBytes(novo.dados, 5, 3)));
            container.atualizaIdLivreControle(1);
        } else {
            if(container.encontrarBlocoPorId(idBlocoLivre).getTamanhoBloco() > tupla.length) {
                container.encontrarBlocoPorId(idBlocoLivre).adicionarTuplaNoBloco(tupla);
            } else {
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