import java.util.ArrayList;
import java.util.List;

public class Container {
    Bloco controle;
    List<Bloco> blocos = new ArrayList<Bloco>();

    public void encontrarBlocoLivre(){

    }

    /* public int getColumsNumber() {
        controle.getBytes(9, )
    }*/

    public Bloco getControle() {
        return controle;
    }

    public void setControle(Bloco controle) {
        this.controle = controle;
    }
}
