import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Buffer {
    private static int[] lru =  new int[40],  memoria =  new int[40];

    private static int[] cacheHitMiss = new int[3];

    private static int idCall, idBuff, controle, pontoMud;

    public static void geraRequisicoes(Container container){
        List<Bloco> blocosRepetidos = new ArrayList<Bloco>();
        Random rand = new Random();
        int idContainer, idBloco;
        int ultimoId = 0;

        for(Bloco bloco : container.blocos) {
            idContainer = bloco.dados[0];
            idBloco = Bloco.byteToInt(Bloco.getBytes(bloco.dados, 1, 3));

            if(idBloco != ultimoId) {
                int repeticoes = rand.nextInt(10) + 1;

                for(int i = 1; i <= repeticoes; i++){
                    blocosRepetidos.add(new Bloco(idBloco, (byte) idContainer));
                }
                ultimoId = idBloco;
            }
        }

        executaBuffer(container, blocosRepetidos);
    }



    public static int[] executaBuffer (Container container, List<Bloco> blocosRepetidos){
        int pos = 0;

        for (Bloco blocoCall : container.blocos)
            for (int blocoBuff : memoria) {
                if(blocoBuff == 0){
                    //Add Miss
                    cacheHitMiss[1]++;

                    //preenche LRU e Memoria
                    lru[controle] = Bloco.byteToInt(Bloco.getBytes(blocoCall.dados, 1, 3));
                    memoria[controle] = Bloco.byteToInt(Bloco.getBytes(blocoCall.dados, 1, 3));
                }
                cacheHitMiss[2]++;
                idCall = Bloco.byteToInt(Bloco.getBytes(blocoCall.dados, 1, 3));
                if (idCall == blocoBuff) {
                    //add Hit
                    cacheHitMiss[0]++;

                    //Implementar LRU
                    lru = ordenaVetorLRU(lru, controle, idCall);
                } else {
                    //Implementar LRU
                    pos = 0;
                    lru = ordenaVetorLRU(lru, pos, idCall);

                    //Atualiza Memoria
                    memoria = ordenaVetorLRU(memoria, pos, idCall);

                    //Add Miss
                    cacheHitMiss[1]++;
                }

                controle++;
            }

        return cacheHitMiss;
    }

    public static int[] ordenaVetorLRU(int[] vecLru, int posicaoMudanca, int idNovo){
        int[] auxLru = vecLru;

        vecLru[posicaoMudanca] = 0;

        for (int i = posicaoMudanca, j = posicaoMudanca + 1; j < vecLru.length; i++, j++) {
            vecLru[i] = auxLru[j];
        }
        vecLru[vecLru.length - 1] = idNovo;

        return vecLru;
    }
}
