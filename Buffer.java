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

    public static Bloco buscaBlocoArquivo (int idBloco, Container container){
        for (Bloco bloco : container.blocos){
            
            if(idBloco == Bloco.byteToInt(Bloco.getBytes(bloco.dados, 1, 3))) return bloco;
                        
        }
        
        
    }    

    public static int[] executaBuffer (Container container, List<Bloco> blocosRepetidos){
        int pos = 0;
        Bloco blocoArq;
        
        for (Bloco blocoCall : blocosRepetidos)            
            //Pega ID do Bloco requisitado
            idCall = Bloco.byteToInt(Bloco.getBytes(blocoCall.dados, 1, 3));
        
            for (int blocoBuff : memoria) {
                if(blocoBuff == 0){
                    //Add Miss
                    cacheHitMiss[1]++;
                    //Pega bloco do arquivo
                    blocoArq = buscaBlocoArquivo(idCall, container);                    
                    //preenche LRU e Memoria
                    lru[controle] = Bloco.byteToInt(Bloco.getBytes(blocoArq.dados, 1, 3));
                    memoria[controle] = Bloco.byteToInt(Bloco.getBytes(blocoArq.dados, 1, 3));
                }   
                //Qtd de chamadas do buffer
                cacheHitMiss[2]++;
                
                if (idCall == blocoBuff) {
                    //add Hit
                    cacheHitMiss[0]++;

                    //Implementar LRU
                    lru = ordenaVetorLRU(lru, controle, idCall);
                } else {
                    
                    //Pega bloco requisitado do arquivo
                    blocoArq = buscaBlocoArquivo(idCall, container); 
                    
                    //Implementar LRU
                    pos = 0;
                    lru = ordenaVetorLRU(lru, pos, Bloco.getBytes(blocoArq.dados, 1, 3));

                    //Atualiza Memoria
                    memoria = ordenaVetorLRU(memoria, pos, Bloco.getBytes(blocoArq.dados, 1, 3));

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
