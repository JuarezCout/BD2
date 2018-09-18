public class LRU {

    Pagina[] lru;

    public LRU (int tam){
        this.lru = new Pagina[tam];
    }

    public Pagina[] getLru() {
        return lru;
    }

    public void setLru(Pagina[] lru) {
        this.lru = lru;
    }
}
