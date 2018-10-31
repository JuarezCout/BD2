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
                byte[] dadosBucket = Bloco.getBytes(bucket0.dados, 0, 8);
                out.write(dadosBucket);
                buckets.add(bucket0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    void adicionarTupla(byte[] tupla, int hash, int idTabela){
        boolean bucketEncontrado = false;
        //procurar bucket na memoria
        for (int i = 0; i < buckets.size(); i++ ) {
            Bloco bucket = buckets.get(i);

            // hash deu bucket 0
            if(bucket.getIdTabela() == idTabela && bucket.getId() == hash && hash == 0){
                bucket.adicionarTuplaNoBlocoCheio(tupla);
                bucketEncontrado = true;
                break;
            }
            //se bucket estiver na memoria e com espaco livre
            if (bucket.getIdTabela() == idTabela && bucket.getId() == hash && Bloco.byteToInt(Bloco.getBytes(bucket.dados, 5, 3)) + tupla.length < limitBlockSize) {
                bucket.adicionarTuplaNoBloco(tupla);
                bucketEncontrado = true;
                break;
            }
            //bucket na memoria mas cheio
            if(bucket.getIdTabela() == idTabela && bucket.getId() == hash){
                System.out.println("Bucket "+ bucket.getId() + " da tabela " + idTabela + " ficou cheio e foi movido para disco");
                bucket.adicionarTuplaNoBlocoCheio(tupla);

                int bytesUsados = Bloco.byteToInt(Bloco.getBytes(bucket.dados, 5, 3));
                byte[] bytesBucket = Bloco.getBytes(bucket.dados, 0 , bytesUsados);
                byte[] arquivo = getArquivoBytes(idTabela);
                byte[] novoArquivo = new byte[arquivo.length + bytesUsados];
                novoArquivo = Bloco.bytePlusbyte(novoArquivo, arquivo, 0);
                novoArquivo = Bloco.bytePlusbyte(novoArquivo, bytesBucket, arquivo.length);
                buckets.remove(i);
                setArquivoBytes(novoArquivo, idTabela);
                bucketEncontrado = true;

                break;
            }
        }

        //procura bucket no disco
        if(bucketEncontrado == false){
            int bucketPosition = procuraBucketDisco(idTabela, hash);
            if(bucketPosition != 0) { //bucket encontrado no disco
                byte[] arquivo = getArquivoBytes(idTabela);
                int posicaoGravacao = bucketPosition + Bloco.byteToInt(Bloco.getBytes(arquivo, bucketPosition + 5, 3));
                byte[] novoArquivo = new byte[arquivo.length + tupla.length];
                byte[] novoTamanho = Bloco.intToByte(Bloco.byteToInt(Bloco.getBytes(arquivo, bucketPosition + 5, 3))+ tupla.length);
                arquivo = Bloco.bytePlusbyte(arquivo, novoTamanho, bucketPosition + 5);

                byte[] parte1 = Bloco.getBytes(arquivo, 0, posicaoGravacao);
                byte[] parte2 = Bloco.getBytes(arquivo, posicaoGravacao, arquivo.length - posicaoGravacao);

                novoArquivo = Bloco.bytePlusbyte(novoArquivo, parte1, 0);
                novoArquivo = Bloco.bytePlusbyte(novoArquivo, tupla, posicaoGravacao);
                novoArquivo = Bloco.bytePlusbyte(novoArquivo, parte2, posicaoGravacao + tupla.length);

                setArquivoBytes(novoArquivo, idTabela);
            } else { // bucket nao existe
                System.out.println("Bucket " + hash + " da tabela " + idTabela + " criado");
                Bloco novoBucket = new Bloco(hash, (byte) idTabela);
                novoBucket.adicionarTuplaNoBloco(tupla);
                adicionaBucket(novoBucket, idTabela);
            }
        }
    }

    void adicionaBucket(Bloco bucketNovo, int idTabela){
        if(verificaLimiteBuckets(idTabela)){ // memoria com espaco livre
            buckets.add(bucketNovo);
        } else { // memoria cheia
            int bucketToDelete = getBucketPositionDelete(idTabela);
            Bloco bucket = buckets.get(bucketToDelete);
            System.out.println("Memoria de buckets da tabela " + idTabela + " ficou cheia");
            System.out.println("Bucket " + bucket.getId() + " foi movido para disco");
            byte[] arquivo = getArquivoBytes(idTabela);
            byte[] novoArquivo = new byte[arquivo.length + bucket.dados.length];

            novoArquivo = Bloco.bytePlusbyte(novoArquivo, arquivo, 0);
            novoArquivo = Bloco.bytePlusbyte(novoArquivo, bucket.dados, arquivo.length);
            setArquivoBytes(novoArquivo, idTabela);

            buckets.remove(bucketToDelete);
            buckets.add(bucketNovo);
        }
    }

    int getBucketPositionDelete(int idTabela){
        for(int i = 0; i < buckets.size(); i++){
            if(buckets.get(i).getIdTabela() == idTabela && buckets.get(i).getId() != 0){
                return i;
            }
        }
        return 2;
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
            i += Bloco.byteToInt(Bloco.getBytes(arquivo, i + 5, 3));

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
