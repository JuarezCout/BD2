import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

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

    ArrayList<String[]> comparaBuckects(HashMap<Integer, int []> selecoes){
        ArrayList<String[]> resultados = new ArrayList<>();

        System.out.println("Iniciando comparações de buckets...");

        for(int i = 0; i < buckets.size(); i++){
            Bloco bucket = buckets.get(i);

            if(bucket.getIdTabela() == 0){
                resultados.addAll(comparaBucketCorrespondente(selecoes, bucket));
            }
        }

        byte[] arquivo = getArquivoBytes(0);

        int i = 0;

        while(i < arquivo.length) {
            byte idContainer = arquivo[i];
            int idBloco = Bloco.byteToInt(Bloco.getBytes(arquivo, i + 1, 3));
            Bloco bucket = new Bloco(idBloco, idContainer);

            bucket.dados = Bloco.getBytes(arquivo, i, Bloco.byteToInt(Bloco.getBytes(arquivo, i + 5, 3)));
            resultados.addAll(comparaBucketCorrespondente(selecoes, bucket));

            i += Bloco.byteToInt(Bloco.getBytes(arquivo, i + 5, 3));
        }

        return resultados;
    }

    ArrayList<String[]> comparaBucketCorrespondente(HashMap<Integer,int[]> selecoes, Bloco bucket) {
        boolean bucketEncontrado = false;
        Bloco bucket2 = null;
        // procura bucket correspondente na memoria
        for(int i = 0; i < buckets.size(); i++){
            bucket2 = buckets.get(i);

            if(bucket2.getIdTabela() == 1 && bucket2.getId() == bucket.getId()){
                bucketEncontrado = true;
                break;
            }
            bucket2 = null;
        }
        // procura bucket correspondente no disco
        if(bucketEncontrado == false){
            bucket2 = getBucketDisco(1, bucket.getId());
        }

        if(bucket2 != null){
            return comparaLinhas(selecoes, bucket, bucket2);
        }
        return null;
    }

    ArrayList<String[]> comparaLinhas(HashMap<Integer,int[]> selecoes, Bloco bucket, Bloco bucket2) {
        int i = 8;
        int tamBucket = bucket.getTamanhoBloco();
        int tamBucket2 = bucket2.getTamanhoBloco();
        ArrayList<String> linha1;
        ArrayList<String> linha2 = new ArrayList<>();
        ArrayList<String[]> resultados = new ArrayList<>();

        while(i < tamBucket){//percorre bucket
            int tamTupla = Bloco.byteToInt(Bloco.getBytes(bucket.dados, i , 3));
            int numColuna = 0;
            int h = i + 4;
            linha1 = new ArrayList<>();

            while(h < tamTupla + i){//percorre tupla
                int tamColuna = Bloco.byte2ToInt(Bloco.getBytes(bucket.dados, h, 2));
                String coluna = Bloco.byteToString(Bloco.getBytes(bucket.dados, h + 2, tamColuna));
                linha1.add(coluna);
                numColuna++;
                h += tamColuna + 2;
            }
            i += tamTupla + numColuna*2 + 4;
            numColuna = 0;

            int numColuna2 = 0;
            int j = 8;
            while(j < tamBucket2){//percorre bucket
                int tamTupla2 = Bloco.byteToInt(Bloco.getBytes(bucket2.dados, j , 3));
                int h2 = j + 4;

                while(h2 < tamTupla2 + j){//percorre tupla
                    int tamColuna2 = Bloco.byte2ToInt(Bloco.getBytes(bucket2.dados, h2, 2));
                    String coluna2 = Bloco.byteToString(Bloco.getBytes(bucket2.dados, h2 + 2, tamColuna2));
                    linha2.add(coluna2);
                    numColuna2++;
                    h2 += tamColuna2 + 2;
                }
                resultados = combinaLinha(linha1, linha2, selecoes, resultados);
                linha2 = new ArrayList<>();
                j += tamTupla2 + numColuna2*2 + 4;
                numColuna2 = 0;
            }
        }
        return resultados;
    }

    ArrayList<String[]> combinaLinha(ArrayList<String> linha1, ArrayList<String> linha2, HashMap<Integer,int[]> selecoes, ArrayList<String[]> resultados) {
        int[] selecoes1 = selecoes.get(0);
        int[] selecoes2 = selecoes.get(1);
        int igualdades = 0;

        for(int i = 0; i < selecoes1.length; i++){
            if(linha1.get(selecoes1[i]).equals(linha2.get(selecoes2[i]))){
                igualdades++;
            }
        }

        if(igualdades == selecoes1.length){
            ArrayList<String> resultado = new ArrayList<>();
            resultado.addAll(linha1);
            resultado.addAll(linha2);

            String[] resultadoCombinado = new String[resultado.size()];
            resultadoCombinado = resultado.toArray(resultadoCombinado);
            resultados.add(resultadoCombinado);
        }

        return resultados;
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

    Bloco getBucketDisco(int idTabela, int hash){
        int bucketPosition = procuraBucketDisco(idTabela, hash);

        if(bucketPosition != 0) {
            byte[] arquivo = getArquivoBytes(idTabela);
            int bytesUsados = Bloco.byteToInt(Bloco.getBytes(arquivo, bucketPosition + 5, 3));
            Bloco bloco = new Bloco(hash, (byte) idTabela);
            byte[] dadosBucket = Bloco.getBytes(arquivo, bucketPosition, bytesUsados);
            bloco.dados = dadosBucket;

            return bloco;
        }

        return null;
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
