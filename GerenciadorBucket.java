import java.io.*;
import java.util.ArrayList;

public class GerenciadorBucket {
    int limitBucketsMemory = 10;
    int limitBlockSize = 8192;
    ArrayList<Bloco> buckets = new ArrayList<>();

    GerenciadorBucket(int numTabelas){
        for (int i = 0; i < numTabelas; i++){
            try {
                FileOutputStream out = new FileOutputStream("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bucketstorage-"+i+".txt");
                Bloco bucket0 = new Bloco(0, (byte) i);
                out.write(bucket0.dados);
                buckets.add(bucket0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    void adicionarTupla(byte[] tupla, int hash, int idTabela){
        //procurar bucket na memoria
        for (int i = 0; i < buckets.size(); i++ ){
            Bloco bucket = buckets.get(i);
            //se bucket estiver na memoria e com espaco livre
            if(bucket.getId() == hash && Bloco.byteToInt(Bloco.getBytes(bucket.dados, 5, 3)) + tupla.length < limitBlockSize) {
                bucket.adicionarTuplaNoBloco(tupla);
                break;
            } else if(bucket.getId() == hash){ //bucket na memoria mas cheio
                System.out.println("Bucket "+ bucket.getId() + " estava cheio e foi movido para disco");
                bucket.adicionarTuplaNoBloco(tupla);
                byte[] arquivo = getArquivoBytes(idTabela);
                arquivo = Bloco.bytePlusbyte(arquivo, bucket.dados, arquivo.length);
                buckets.remove(i);
                setArquivoBytes(arquivo, idTabela);
                break;
            } else { //procura bucket no disco
                int bucketPosition = procuraBucketDisco(idTabela, hash);
                if(bucketPosition != 0) { //bucket encontrado no disco
                    byte[] arquivo = getArquivoBytes(idTabela);
                    int posicaoGravacao = bucketPosition + Bloco.byteToInt(Bloco.getBytes(arquivo, 5, 3));
                    byte[] novoArquivo = Bloco.getBytes(arquivo, 0, posicaoGravacao);
                    novoArquivo = Bloco.bytePlusbyte(novoArquivo, tupla, posicaoGravacao);
                    novoArquivo = Bloco.bytePlusbyte(novoArquivo, arquivo, posicaoGravacao + tupla.length);
                    setArquivoBytes(novoArquivo, idTabela);
                    break;
                } else { // bucket nao existe
                    Bloco novoBucket = new Bloco(hash, (byte) idTabela);
                    novoBucket.adicionarTuplaNoBloco(tupla);
                    adicionaBucket(novoBucket, idTabela);
                    break;
                }
            }
        }
    }

    void adicionaBucket(Bloco bucketNovo, int idTabela){
        if(verificaLimiteBuckets(idTabela)){ // memoria com espaco livre
            buckets.add(bucketNovo);
        } else { // memoria cheia
            Bloco bucket = buckets.get(1);
            byte[] arquivo = getArquivoBytes(idTabela);
            arquivo = Bloco.bytePlusbyte(arquivo, bucket.dados, arquivo.length);
            buckets.remove(1);
            setArquivoBytes(arquivo, idTabela);
            buckets.add(1, bucketNovo);
        }
    }

    boolean verificaLimiteBuckets(int idTabela){
        int contagem = 0;

        for(int i = 0; i < buckets.size(); i++ ){
            if(buckets.get(i).getIdTabela() == idTabela){
                contagem++;
            }
        }

        if(contagem < limitBucketsMemory) {
            return true;
        } else {
            return false;
        }
    }

    int procuraBucketDisco(int idTabela, int hash){
        byte[] arquivo = getArquivoBytes(idTabela);
        int i = 0;

        while(i < arquivo.length) {
            if(Bloco.byteToInt(Bloco.getBytes(arquivo, i+1, 3)) == hash) {
                return i;
            }
            i += Bloco.byteToInt(Bloco.getBytes(arquivo, i+5, 3));

            if(arquivo.length == 8192) {
                return 0;
            }
        }

        return 0;
    }

    void setArquivoBytes(byte[] arquivo, int idTabela){
        try {
            FileOutputStream out = new FileOutputStream("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bucketstorage-"+idTabela+".txt");
            out.write(arquivo);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    byte[] getArquivoBytes(int idTabela) {
        File file = new File("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bucketstorage-"+idTabela+".txt");
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesArray;
    }

}
