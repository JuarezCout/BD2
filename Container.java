import java.util.ArrayList;
import java.util.List;

public class Container {
    static int containerId = 0;
    Bloco controle;
    List<Bloco> blocos = new ArrayList<Bloco>();

    Container(String linha){
        containerId++;
        controle = new Bloco(linha, (byte) containerId);
    }

    public Bloco encontrarBlocoPorId(int id){
        for(int i = 0; i < blocos.size(); i++) {
            Bloco b = blocos.get(i);
            if (id == Bloco.byteToInt(Bloco.getBytes(b.dados, 1, 3))){
                return b;
            }
        }
        return null;
    }

    public byte getContainerId(){
        return  controle.dados[0];
    }


    public void atualizaIdLivreControle(int id) {
        controle.setBytes(Bloco.intToByte(id), 5, 3);
    }

    public int tamanhoDoBloco(){
        return Bloco.byteToInt(Bloco.getBytes(controle.dados, 1, 3));
    }

    public Bloco getControle() {
        return controle;
    }

    public void setControle(Bloco controle) {
        this.controle = controle;
    }
}
