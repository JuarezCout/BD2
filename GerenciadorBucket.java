import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GerenciadorBucket {
    //ArrayList<String[]> resultados;
    int limitBucketsMemory = 20;
    int limitBlockSize = 16184;
    ArrayList<Bloco> buckets = new ArrayList<>();
    ArrayList<byte[]> bytesArquivos = new ArrayList<>();
    boolean bucketMontagem = false;
    boolean executaProbe = true;
    boolean bucketEmDisco = false;
    BlockingQueue<String> filaProbeMemoria = new LinkedBlockingDeque<>();
    BlockingQueue<Integer> filaProbeDisco = new LinkedBlockingDeque<>();
    HashMap<Integer, Integer> probeTabela1 = new HashMap<>();
    HashMap<Integer, Integer> probeTabela2 = new HashMap<>();
    Semaphore acessoBucket = new Semaphore(1);

    GerenciadorBucket(int numTabelas){
        for (int i = 0; i < numTabelas; i++){
            try {
                FileOutputStream out = new FileOutputStream("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bucketstorage-"+i+".txt");
                Bloco bucket0 = new Bloco(0, (byte) i);
                byte[] dadosBucket = Bloco.getBytes(bucket0.dados, 0, 8);
                out.write(dadosBucket);
                out.close();
                bytesArquivos.add(dadosBucket);
                buckets.add(bucket0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void criaThreadProbe(){
        new Thread() {
            @Override
            public void run() {
                //resultados = new ArrayList<>();
                System.out.println();
                System.out.println("Iniciando thread de probe...");
                while(executaProbe) {
                    try {
                        if(Interface.tabelasBuildadas == 2 && filaProbeMemoria.isEmpty()){
                            System.out.println();
                            System.out.println("Fim das comparações de buckets em memória...");
                            break;
                        }

                        String hash = filaProbeMemoria.poll(50L, TimeUnit.MILLISECONDS);

                        if(hash != null) {
                            try {
                                acessoBucket.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            System.out.println();
                            System.out.println("Comparando buckets de hash " + hash);
                            comparaBuckets(Integer.valueOf(hash), "m");

                            acessoBucket.release();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while(executaProbe) {
                    try {
                        if(filaProbeDisco.isEmpty()){
                            System.out.println();
                            System.out.println("Fim das comparações de buckets em disco...");
                            break;
                        }

                        int hash = filaProbeDisco.take();

                        try {
                            acessoBucket.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println();
                        System.out.println("Comparando buckets de hash " + hash);
                        comparaBuckets(hash, "d");

                        acessoBucket.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    void comparaBuckets(int hash, String location) {
        boolean bucketEncontrado = false;
        //System.out.println();
        //System.out.println("Iniciando comparações de buckets...");

        if (location == "m") {
            for (int i = 0; i < buckets.size(); i++) {
                Bloco bucket = buckets.get(i);

                if (bucket.getIdTabela() == 0 && bucket.getId() == hash) {
                    comparaBucketCorrespondente(Interface.selecoes, bucket);
                    bucketEncontrado = true;
                    break;
                }
            }

        } else {

            byte[] arquivo = getArquivoBytes(0);

            int i = 0;

            while (i < arquivo.length) {
                if (bucketEncontrado) {
                    break;
                }

                byte idContainer = arquivo[i];
                int idBloco = Bloco.byteToInt(Bloco.getBytes(arquivo, i + 1, 3));
                Bloco bucket = new Bloco(idBloco, idContainer);

                bucket.dados = Bloco.getBytes(arquivo, i, Bloco.byteToInt(Bloco.getBytes(arquivo, i + 5, 3)));

                if (idBloco == hash) {
                    comparaBucketCorrespondente(Interface.selecoes, bucket);
                    break;
                }

                i += Bloco.byteToInt(Bloco.getBytes(arquivo, i + 5, 3));
            }
        }

        //System.out.println();
        //System.out.println("Fim das comparações de buckets");

    }

    void comparaBucketCorrespondente(HashMap<Integer,int[]> selecoes, Bloco bucket) {
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
            comparaLinhas(selecoes, bucket, bucket2);
        }
    }

    void comparaLinhas(HashMap<Integer,int[]> selecoes, Bloco bucket, Bloco bucket2) {
        int i = 8;
        int j = 8;
        int tamBucket = bucket.getTamanhoBloco();
        int tamBucket2 = bucket2.getTamanhoBloco();
        int numExecucoes = 0;
        ArrayList<String> linha1;
        ArrayList<String> linha2 = new ArrayList<>();

        while(numExecucoes < 2) {
            i = getInicioProbeBucket(0, bucket.getId());
            j = getInicioProbeBucket(1, bucket2.getId());

            if (i == 8 && j == 8) {
                numExecucoes++;
            } else if (i < tamBucket && numExecucoes == 0) {
                j = 8;
            } else if (j < tamBucket2 && numExecucoes == 1) {
                if (i < tamBucket) {
                    tamBucket = i;
                }
                i = 8;
            } else if(i == tamBucket && j == tamBucket2){
                break;
            }


            while (i < tamBucket) {//percorre bucket
                int tamTupla = Bloco.byteToInt(Bloco.getBytes(bucket.dados, i, 3));
                int numColuna = 0;
                int h = i + 4;
                linha1 = new ArrayList<>();

                while (h < tamTupla + i) {//percorre tupla
                    int tamColuna = Bloco.byte2ToInt(Bloco.getBytes(bucket.dados, h, 2));
                    String coluna = Bloco.byteToString(Bloco.getBytes(bucket.dados, h + 2, tamColuna));
                    linha1.add(coluna);
                    numColuna++;
                    h += tamColuna + 2;
                }

                i += tamTupla + numColuna * 2 + 4;
                numColuna = 0;

                int tamTupla2;
                int h2;
                int numColuna2 = 0;
                while (j < tamBucket2) {//percorre bucket
                    tamTupla2 = Bloco.byteToInt(Bloco.getBytes(bucket2.dados, j, 3));
                    h2 = j + 4;

                    while (h2 < tamTupla2 + j) {//percorre tupla
                        int tamColuna2 = Bloco.byte2ToInt(Bloco.getBytes(bucket2.dados, h2, 2));
                        String coluna2 = Bloco.byteToString(Bloco.getBytes(bucket2.dados, h2 + 2, tamColuna2));
                        linha2.add(coluna2);
                        numColuna2++;
                        h2 += tamColuna2 + 2;
                    }
                    combinaLinha(linha1, linha2, selecoes);
                    linha2 = new ArrayList<>();
                    j += tamTupla2 + numColuna2 * 2 + 4;
                    numColuna2 = 0;
                }
                //resultados.trimToSize();
            }
            numExecucoes ++;
        }

        probeTabela1.put(bucket.getId(), i);
        probeTabela2.put(bucket2.getId(), j);
    }

    void combinaLinha(ArrayList<String> linha1, ArrayList<String> linha2, HashMap<Integer,int[]> selecoes) {
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
            Interface.data.add(resultadoCombinado);
        }

    }

    int getInicioProbeBucket(int idTabela, int idBucket){
        int limite = 0;

        if(idTabela == 0 && probeTabela1.containsKey(idBucket)){
            limite = probeTabela1.get(idBucket);
        } else if(idTabela == 1 && probeTabela2.containsKey(idBucket)){
            limite = probeTabela2.get(idBucket);
        }

        if(limite != 0){
            return limite;
        }

        return 8;
    }


    void adicionarTupla(byte[] tupla, int hash, int idTabela){
        boolean bucketEncontrado = false;

        try {
            acessoBucket.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
                bucket.adicionarTuplaNoBlocoCheio(tupla);
                bucketEncontrado = true;
                break;
            }
            //bucket na memoria mas cheio
            if(bucket.getIdTabela() == idTabela && bucket.getId() == hash){
                System.out.println();
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

                adicionaBucketCorrespondenteNoDisco(idTabela, hash);

                bucketEncontrado = true;
                bucketEmDisco = true;

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

                if (!filaProbeDisco.contains(hash)) {
                    try {
                        filaProbeDisco.put(hash);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                bucketEmDisco = true;
            } else { // bucket nao existe
                System.out.println();
                System.out.println("Bucket " + hash + " da tabela " + idTabela + " criado");
                Bloco novoBucket = new Bloco(hash, (byte) idTabela);
                novoBucket.adicionarTuplaNoBloco(tupla);
                adicionaBucket(novoBucket, idTabela);
            }
        }

        // adiciona na fila de probe
        if(!bucketEmDisco) {
            if (!filaProbeMemoria.contains(hash)) {
                try {
                    filaProbeMemoria.put(String.valueOf(hash));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        bucketEmDisco = false;

        acessoBucket.release();
    }

    void adicionaBucket(Bloco bucketNovo, int idTabela){
        if(verificaLimiteBuckets(idTabela)){ // memoria com espaco livre
            buckets.add(bucketNovo);
        } else { // memoria cheia
            int bucketToDelete = getBucketPositionDelete(idTabela);
            Bloco bucket = buckets.get(bucketToDelete);
            int idBucket = bucket.getId();
            System.out.println();
            System.out.println("Memoria de buckets da tabela " + idTabela + " ficou cheia");
            System.out.println("Bucket " + bucket.getId() + " foi movido para disco");

            int bytesUsados = Bloco.byteToInt(Bloco.getBytes(bucket.dados, 5, 3));
            byte[] bytesBucket = Bloco.getBytes(bucket.dados, 0 , bytesUsados);

            byte[] arquivo = getArquivoBytes(idTabela);
            byte[] novoArquivo = new byte[arquivo.length + bytesUsados];

            novoArquivo = Bloco.bytePlusbyte(novoArquivo, arquivo, 0);
            novoArquivo = Bloco.bytePlusbyte(novoArquivo, bytesBucket, arquivo.length);
            setArquivoBytes(novoArquivo, idTabela);

            adicionaBucketCorrespondenteNoDisco(idTabela, idBucket);

            bucketEmDisco = true;

            buckets.remove(bucketToDelete);
            buckets.add(bucketNovo);
        }
    }

    void adicionaBucketCorrespondenteNoDisco(int idTabela, int hash) {
        boolean bucketEncontrado = false;

        if(idTabela == 0){
            idTabela = 1;
        } else {
            idTabela = 0;
        }

        for (int i = 0; i < buckets.size(); i++ ) {
            Bloco bucket = buckets.get(i);

            if(bucket.getId() == hash && bucket.getIdTabela() == idTabela) {
                System.out.println();
                System.out.println("Bucket correspondente de hash " + hash + " da tabela " + idTabela + " foi movido para disco");

                int bytesUsados = Bloco.byteToInt(Bloco.getBytes(bucket.dados, 5, 3));
                byte[] bytesBucket = Bloco.getBytes(bucket.dados, 0 , bytesUsados);

                byte[] arquivo = getArquivoBytes(idTabela);
                byte[] novoArquivo = new byte[arquivo.length + bytesUsados];

                novoArquivo = Bloco.bytePlusbyte(novoArquivo, arquivo, 0);
                novoArquivo = Bloco.bytePlusbyte(novoArquivo, bytesBucket, arquivo.length);
                setArquivoBytes(novoArquivo, idTabela);

                buckets.remove(i);

                if(filaProbeMemoria.contains(String.valueOf(hash))) {
                    filaProbeMemoria.remove(String.valueOf(hash));
                }

                filaProbeDisco.add(hash);
                bucketEncontrado = true;

                break;
            }
        }

        if(!bucketEncontrado){
            System.out.println();
            System.out.println("Bucket correspondente de hash " + hash + " da tabela " + idTabela + " foi movido para disco");

            Bloco novoBucket = new Bloco(hash, (byte) idTabela);

            int bytesUsados = Bloco.byteToInt(Bloco.getBytes(novoBucket.dados, 5, 3));
            byte[] bytesBucket = Bloco.getBytes(novoBucket.dados, 0 , bytesUsados);
            byte[] arquivo = getArquivoBytes(idTabela);
            byte[] novoArquivo = new byte[arquivo.length + bytesUsados];
            novoArquivo = Bloco.bytePlusbyte(novoArquivo, arquivo, 0);
            novoArquivo = Bloco.bytePlusbyte(novoArquivo, bytesBucket, arquivo.length);

            setArquivoBytes(novoArquivo, idTabela);
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

    void apagaBuckets(int numTabelas){
        System.out.println();
        System.out.println("Apagando buckets...");

        buckets.clear();
        for(int i = 0; i < numTabelas; i++) {
            try {
                FileOutputStream out = new FileOutputStream("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bucketstorage-" + i + ".txt");
                Bloco bucket0 = new Bloco(0, (byte) i);
                byte[] dadosBucket = Bloco.getBytes(bucket0.dados, 0, 8);
                out.write(dadosBucket);
                buckets.add(bucket0);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void setBucketMontagem(boolean status){
        if(status == true){
            bucketMontagem = true;
        } else {
            bucketMontagem = false;
            for(int i = 0; i < bytesArquivos.size(); i++){
                setArquivoBytes(bytesArquivos.get(i), i);
            }
        }
    }

    /*
    void limpaMemoria(){
        resultados = null;
    }
    */

    void setArquivoBytes(byte[] arquivo, int idTabela){

        if(bucketMontagem == true){
            bytesArquivos.set(idTabela, arquivo);
        } else {
            try {
                FileOutputStream out = new FileOutputStream("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bucketstorage-" + idTabela + ".txt");
                out.write(arquivo);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    byte[] getArquivoBytes(int idTabela) {
        byte[] bytesArray = new byte[1];

        if(bucketMontagem == true){
            return bytesArquivos.get(idTabela);
        } else {
            try {
                File file = new File("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bucketstorage-" + idTabela + ".txt");
                bytesArray = new byte[(int) file.length()];

                FileInputStream fis = null;
                fis = new FileInputStream(file);
                fis.read(bytesArray);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bytesArray;
        }
    }

}
