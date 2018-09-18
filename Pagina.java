public class Pagina {

    private int fileID;
    private int blocoID;
    private int pos;

    public Pagina(int fileID, int blocoID, int pos) {
        this.fileID = fileID;
        this.blocoID = blocoID;
        this.pos = pos;
    }

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    public int getBlocoID() {
        return blocoID;
    }

    public void setBlocoID(int blocoID) {
        this.blocoID = blocoID;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
