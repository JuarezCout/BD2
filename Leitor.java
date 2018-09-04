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
                Tupla.montaTuplaByte(separador(linha));
           }


            arquivo.close();

        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    String[] separador(String linha){
        return linha.split("\\|");
    }
}