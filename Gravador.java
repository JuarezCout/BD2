import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Gravador {
    public static void salvaArquivo(Container container) {

        int tamanho = Bloco.byteToInt(Bloco.getBytes(container.controle.dados, 1, 3));

        byte[] bytes = new byte[tamanho + (container.blocos.size() * tamanho)];

        bytes = Bloco.bytePlusbyte(bytes, container.controle.dados, 0);

        for(int i = 0, j = 2048; i < container.blocos.size(); i++, j += 2048 ) {
            bytes = Bloco.bytePlusbyte(bytes, container.blocos.get(i).dados, j);
        }


        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bd.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            stream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
