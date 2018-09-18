public class Buffer {

    // Criar estrutura pro Buffer
    int teste;

    Bloco[] buffer;

    public Buffer(int tam){
        this.buffer = new Bloco[tam];
    }

    public Bloco[] getBuffer() {
        return buffer;
    }

    public void setBuffer(Bloco[] buffer) {
        this.buffer = buffer;
    }
}
