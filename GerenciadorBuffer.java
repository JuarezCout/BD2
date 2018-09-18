import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GerenciadorBuffer {

    //Declaração
    private static LRU lru = new LRU(20);
    private static Buffer buffer = new Buffer(20);


    private static int[] cacheHitMiss = new int[3];

    private static int idBloco, idCont, idBuff, idRequisicaoCont, idRequisicaoBloco, controle, pontoMud;

    public static void geraRequisicoes(){
        List<Pagina> paginasRepetidas = new ArrayList<Pagina>();
        Random rand = new Random();
        int idContainer, idBloco;
        int ultimoId = 0;

        for (Container container: Leitor.containers) {
            for(Bloco bloco : container.blocos) {
                idContainer = bloco.dados[0];
                idBloco = Bloco.byteToInt(Bloco.getBytes(bloco.dados, 1, 3));
                Pagina p = new Pagina(idContainer, idBloco, 0);

                if(idBloco != ultimoId) {
                    int repeticoes = rand.nextInt(10) + 1;

                    for(int i = 1; i <= repeticoes; i++){
                        paginasRepetidas.add(p);
                    }
                    ultimoId = idBloco;
                }
            }
        }

        executaBuffer(paginasRepetidas);
    }

    public static Bloco buscaBlocoArquivo (Pagina pagina){

        for (Container container: Leitor.containers) {
            if (pagina.getFileID() == container.controle.dados[0]) {
                for (Bloco bloco : container.blocos) {
                    if (pagina.getBlocoID() == Bloco.byteToInt(Bloco.getBytes(bloco.dados, 1, 3))) return bloco;
                }
            }
        }

        return null;
    }    

    public static int[] executaBuffer (List<Pagina> paginasReq){
        int pos = 0;
        Bloco blocoArq;
        
        for (Pagina pagina : paginasReq) {
            //Pega HowID do Bloco requisitado
            idRequisicaoCont  = pagina.getFileID();
            idRequisicaoBloco = pagina.getBlocoID();


            for (Bloco blocoBuff : buffer.getBuffer()) {
                //Qtd de chamadas do buffer
                cacheHitMiss[2]++;

                if (idRequisicaoCont  == Bloco.byteToInt(Bloco.getBytes(blocoBuff.dados, 0, 1)) &&
                    idRequisicaoBloco == Bloco.byteToInt(Bloco.getBytes(blocoBuff.dados, 1, 3))) {
                    //add Hit
                    cacheHitMiss[0]++;

                    //Implementar LRU
                    lru.setLru(ordenaVetorLRU(lru.getLru(), controle, pagina));
                } else {

                    //Pega bloco requisitado do arquivo
                    blocoArq = buscaBlocoArquivo(pagina);

                    //Implementar LRU
                    pos = 0;
                    lru.setLru(ordenaVetorLRU(lru.getLru(), pos, pagina));

                    //Atualiza Memoria
                    buffer.setBuffer(ordenaVetorBuffer(buffer.getBuffer(), pos, blocoArq));

                    //Add Miss
                    cacheHitMiss[1]++;
                }

                controle++;
            }

        }

        return cacheHitMiss;
    }

    public static Pagina[] ordenaVetorLRU(Pagina[] vecLru, int posicaoMudanca, Pagina pgNovo){
        Pagina[] auxLru = vecLru;

        vecLru[posicaoMudanca] = null;

        for (int i = posicaoMudanca, j = posicaoMudanca + 1; j < vecLru.length; i++, j++) {
            vecLru[i] = auxLru[j];
        }
        vecLru[vecLru.length - 1] = pgNovo;

        return vecLru;
    }

    public static Bloco[] ordenaVetorBuffer(Bloco[] vecBuffer, int posicaoMudanca, Bloco blNovo){
        Bloco[] auxBuffer = vecBuffer;

        vecBuffer[posicaoMudanca] = null;

        for (int i = posicaoMudanca, j = posicaoMudanca + 1; j < vecBuffer.length; i++, j++) {
            vecBuffer[i] = auxBuffer[j];
        }
        vecBuffer[vecBuffer.length - 1] = blNovo;

        return vecBuffer;
    }
}
