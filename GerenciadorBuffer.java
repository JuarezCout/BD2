import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GerenciadorBuffer {

    //Declaração
    private static LRU lru = new LRU(20);
    private static Buffer buffer = new Buffer(20);


    private static int[] cacheHitMiss = new int[3];

    private static int idBloco, idCont, idBuff, idRequisicaoCont, idRequisicaoBloco, controle = -1, pontoMud;

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
        int posLRU = 0;
        int posBuff = 0;
        Bloco blocoArq;
        
        for (Pagina pagina : paginasReq) {
            //Pega HowID do Bloco requisitado
            idRequisicaoCont  = pagina.getFileID();
            idRequisicaoBloco = pagina.getBlocoID();

            System.out.println("Buscando o bloco: " + idRequisicaoCont + "-" + idRequisicaoBloco);
            System.out.println();

            //Qtd de chamadas do buffer
            cacheHitMiss[2]++;

            if (buffer.getBuffer()[0] == null) {

                //Pega bloco requisitado do arquivo
                blocoArq = buscaBlocoArquivo(pagina);

                //Atualiza Memoria
                buffer.setBuffer(substituiVetorBuffer(buffer.getBuffer(), 0, blocoArq));

                //Implementar LRU
                lru.setLru(ordenaVetorLRU(lru.getLru(), 0, 0, pagina));

                //Add Miss
                cacheHitMiss[1]++;
                System.out.println("Miss: " + cacheHitMiss[1]);

                continue;

            }

            for (Bloco blocoBuff : buffer.getBuffer()) {
                controle++;
                //Verificação de existência do Bloco no buffer
                if (idRequisicaoCont  == blocoBuff.dados[0]  &&
                    idRequisicaoBloco == Bloco.byteToInt(Bloco.getBytes(blocoBuff.dados, 1, 3))) {
                    //add Hit
                    cacheHitMiss[0]++;
                    System.out.println("Hit: " + cacheHitMiss[0]);

                    //Implementar LRU
                    lru.setLru(ordenaVetorLRU(lru.getLru(), controle, controle, pagina));

                    continue;
                } else {

                    //Pega bloco requisitado do arquivo
                    blocoArq = buscaBlocoArquivo(pagina);

                    //Pega posicao do Buffer que vai sair de acordo com a LRU
                    if (lru.getLru().length != 20) {
                        posBuff = lru.getLru().length;
                    } else {
                        posBuff = pegaPosicaoBuffer(lru.getLru(), idRequisicaoBloco, idRequisicaoCont);
                    }

                    //Atualiza Memoria
                    if (buffer.getBuffer().length == 20){
                        buffer.setBuffer(substituiVetorBuffer(buffer.getBuffer(), posBuff, blocoArq));
                    } else{
                        buffer.setBuffer(substituiVetorBuffer(buffer.getBuffer(), buffer.getBuffer().length, blocoArq));
                    }

                    //Implementar LRU
                    posLRU = 0;
                    lru.setLru(ordenaVetorLRU(lru.getLru(), posLRU, posBuff, pagina));

                    //Add Miss
                    cacheHitMiss[1]++;
                    System.out.println("Miss: " + cacheHitMiss[1]);

                    continue;
                }
            }

        }

        return cacheHitMiss;
    }

    public static Pagina[] ordenaVetorLRU(Pagina[] vecLru, int posicaoMudanca, int posiBuffer, Pagina pgNovo){
        Pagina[] auxLru = vecLru;

        vecLru[posicaoMudanca] = null;

        for (int i = posicaoMudanca, j = posicaoMudanca + 1; j < vecLru.length; i++, j++) {
            vecLru[i] = auxLru[j];
        }
        pgNovo.setPos(posiBuffer);
        vecLru[vecLru.length - 1] = pgNovo;

        return vecLru;
    }

    public static Bloco[] substituiVetorBuffer(Bloco[] vecBuffer, int posicaoMudanca, Bloco blNovo){
        Bloco[] auxBuffer = vecBuffer;

        vecBuffer[posicaoMudanca] = null;

        vecBuffer[posicaoMudanca] = blNovo;

        return vecBuffer;
    }

    public static int pegaPosicaoBuffer (Pagina[] lru, int idBloco, int idCont){

        for (Pagina pagina : lru ) {
            if (pagina.getFileID() == idCont && pagina.getBlocoID() == idBloco){
                return pagina.getPos();
            }
        }

        return 0;
    }
}
