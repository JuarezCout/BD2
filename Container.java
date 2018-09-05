import java.util.ArrayList;
import java.util.List;

public class Container {
    Bloco controle;
    List<Bloco> blocos = new ArrayList<Bloco>();

    public Bloco encontrarBlocoPorId(int id){
        for(int i = 0; i < blocos.size(); i++) {
            Bloco b = blocos.get(i);
            System.out.println("for " + b.dados[0]);
            System.out.println("for " + b.dados[1]);
            System.out.println("for " + b.dados[2]);
            System.out.println("for " + b.dados[3]);
            if (id == Bloco.byteToInt(Bloco.getBytes(b.dados, 1, 3))){
                return b;
            }
        }
        return null;
    }

    public void atualizaIdLivreControle(int id) {
        controle.setBytes(Bloco.intToByte(id), 5, 3);
        System.out.println("id"+Bloco.byteToInt(Bloco.getBytes(controle.dados, 5, 3)));
    }

    public Bloco getControle() {
        return controle;
    }

    public void setControle(Bloco controle) {
        this.controle = controle;
    }
}
