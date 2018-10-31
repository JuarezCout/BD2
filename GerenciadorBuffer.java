import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.text.DecimalFormat;

public class GerenciadorBuffer {

    //Declaração
    private static LRU lru = new LRU(680);
    private static Buffer buffer = new Buffer(680);


    private static int[] cacheHitMiss = new int[3];

    private static int idBloco, idCont, idBuff, idRequisicaoCont, idRequisicaoBloco, controle = -1, pontoMud;

    public static void geraRequisicoes(){
        List<Pagina> paginasRepetidas = new ArrayList<Pagina>();
        Random rand = new Random();
        int idContainer, idBloco;

        for (Container container: Leitor.containers) {
            for(Bloco bloco : container.blocos) {
                idContainer = bloco.dados[0];
                idBloco = Bloco.byteToInt(Bloco.getBytes(bloco.dados, 1, 3));
                Pagina p = new Pagina(idContainer, idBloco, 0);

                if(idBloco % 2 == 0){
                    int repeticoes = rand.nextInt(paginasRepetidas.size());

                    for(int i = 1; i <= 3; i++){
                        paginasRepetidas.add(paginasRepetidas.get(repeticoes));
                    }


                }

                int repeticoes = rand.nextInt(2) + 1;

                for(int i = 1; i <= 3; i++){
                    paginasRepetidas.add(p);
                }

            }
        }

        executaBuffer(paginasRepetidas);


    }

    public static void exibirResultados() {
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println("Tamanho da Memória: 	" + lru.getLru().length);
        System.out.println("Quantidade de Paginas requisitadas:	" + cacheHitMiss[2]);
        System.out.println("Cache Hit: 		" + cacheHitMiss[0]);
        System.out.println("Cache Miss: 		" + cacheHitMiss[1]);
        System.out.println("Taxa de Hit:		"
                + df.format(((double) cacheHitMiss[0] / cacheHitMiss[2]) * 100) + "%");
        System.out
                .println("Taxa de Miss:		"
                        + df.format(((double) cacheHitMiss[1] / cacheHitMiss[2]) * 100)
                        + "%");
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

    public static Bloco getBlocoBuffer (Pagina paginaReq){
        Bloco blocoArq;


            //Pega HowID do Bloco requisitado
            idRequisicaoCont  = paginaReq.getFileID();
            idRequisicaoBloco = paginaReq.getBlocoID();
            int status = 0;

            System.out.println("Buscando o bloco: " + idRequisicaoCont + "-" + idRequisicaoBloco);
            System.out.println();

            //Qtd de chamadas do buffer
            cacheHitMiss[2]++;
            //verifica se buffer esta vazio
            if (buffer.getBuffer()[0] == null) {

                //Pega bloco requisitado do arquivo
                blocoArq = buscaBlocoArquivo(paginaReq);

                //Atualiza Memoria
                buffer.setBuffer(substituiVetorBuffer(buffer.getBuffer(), 0, blocoArq));

                //Implementar LRU
                adicionaPaginaLRU(paginaReq);
                //lru.setLru(ordenaVetorLRU(lru.getLru(), 0, 0, paginaReq));

                //Add Miss
                cacheHitMiss[1]++;
                //System.out.println("Miss: " + cacheHitMiss[1]);

                return blocoArq;

            }
            //procura bloco no buffer
            for (Bloco blocoBuff : buffer.getBuffer()) {
                controle++;

                if(blocoBuff == null){
                    break;
                }

                //Verificação de existência do Bloco no buffer
                if (idRequisicaoCont  == blocoBuff.dados[0]  &&
                        idRequisicaoBloco == Bloco.byteToInt(Bloco.getBytes(blocoBuff.dados, 1, 3))) {
                    //add Hit
                    cacheHitMiss[0]++;
                    //System.out.println("Hit: " + cacheHitMiss[0]);

                    //Implementar LRU
                    ordenaLRU(paginaReq);
                    //lru.setLru(ordenaVetorLRU(lru.getLru(), controle, controle, paginaReq));
                    status = 1;
                    return blocoBuff;
                }
            }

            if(status == 0) {//bloco não encontrado no buffer

                //Pega bloco requisitado do arquivo
                blocoArq = buscaBlocoArquivo(paginaReq);

                paginaReq = adicionaBlocoBuffer(blocoArq, paginaReq);

                //Implementar LRU
                adicionaPaginaLRU(paginaReq);
                //lru.setLru(ordenaVetorLRU(lru.getLru(), posLRU, posBuff, paginaReq));

                //Add Miss
                cacheHitMiss[1]++;
                //System.out.println("Miss: " + cacheHitMiss[1]);

                status = 0;
                return blocoArq;
            }
            status = 0;


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
            int status = 0;

            System.out.println("Buscando o bloco: " + idRequisicaoCont + "-" + idRequisicaoBloco);
            System.out.println();

            //Qtd de chamadas do buffer
            cacheHitMiss[2]++;
            //verifica se buffer esta vazio
            if (buffer.getBuffer()[0] == null) {

                //Pega bloco requisitado do arquivo
                blocoArq = buscaBlocoArquivo(pagina);

                //Atualiza Memoria
                buffer.setBuffer(substituiVetorBuffer(buffer.getBuffer(), 0, blocoArq));

                //Implementar LRU
                adicionaPaginaLRU(pagina);
                //lru.setLru(ordenaVetorLRU(lru.getLru(), 0, 0, pagina));

                //Add Miss
                cacheHitMiss[1]++;
                System.out.println("Miss: " + cacheHitMiss[1]);

                continue;

            }
            //procura bloco no buffer
            for (Bloco blocoBuff : buffer.getBuffer()) {
                controle++;

                if(blocoBuff == null){
                    break;
                }

                //Verificação de existência do Bloco no buffer
                if (idRequisicaoCont  == blocoBuff.dados[0]  &&
                    idRequisicaoBloco == Bloco.byteToInt(Bloco.getBytes(blocoBuff.dados, 1, 3))) {
                    //add Hit
                    cacheHitMiss[0]++;
                    System.out.println("Hit: " + cacheHitMiss[0]);

                    //Implementar LRU
                    ordenaLRU(pagina);
                    //lru.setLru(ordenaVetorLRU(lru.getLru(), controle, controle, pagina));
                    status = 1;
                    break;
                }
            }

            if(status == 0) {//bloco não encontrado no buffer

                //Pega bloco requisitado do arquivo
                blocoArq = buscaBlocoArquivo(pagina);

                pagina = adicionaBlocoBuffer(blocoArq, pagina);

                //Implementar LRU
                adicionaPaginaLRU(pagina);
                //lru.setLru(ordenaVetorLRU(lru.getLru(), posLRU, posBuff, pagina));

                //Add Miss
                cacheHitMiss[1]++;
                System.out.println("Miss: " + cacheHitMiss[1]);

                status = 0;
            }
            status = 0;

        }

        exibirResultados();
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

    public static void ordenaLRU(Pagina pgNovo){
        Pagina[] vecLru = lru.getLru();
        Pagina[] aux = vecLru.clone();
        int espacoLivre = 0;

        for(int i = 0; i < vecLru.length; i++){
            if(vecLru[i] == null){
                espacoLivre++;
            }
        }

        for(int i = 0; i < vecLru.length - espacoLivre; i++){

            if(vecLru[i].getFileID() == pgNovo.getFileID() && vecLru[i].getBlocoID() == pgNovo.getBlocoID()){
                if(i == 0){
                    break;
                }
                aux[0] = vecLru[i];
                for(int j = 0, g = 0; j < vecLru.length - espacoLivre - 1; j++, g++){
                    if(vecLru[i].getFileID() == vecLru[j].getFileID() && vecLru[i].getBlocoID() == vecLru[j].getBlocoID()){
                        //aux[j + 1] = vecLru[j + 1];
                        g++;
                    }
                    aux[j + 1] = vecLru[g];
                }
                break;
            }
        }
        lru.setLru(aux);
    }

    public static void adicionaPaginaLRU(Pagina pgNovo){
        int espacoLivre = 0;
        Pagina[] vecLru = lru.getLru().clone();
        //verifica numero de espacos livres na lru
        for(int i = 0; i < vecLru.length; i++){
            if(vecLru[i] == null){
                espacoLivre++;
            }
        }
        //lru esta vazio
        if(espacoLivre == vecLru.length){
            vecLru[0] = pgNovo;
        } else{//lru nao esta vazio
            Pagina[] aux = lru.getLru().clone();
                for(int i = 1, j = 0; i < vecLru.length; i++, j++){
                    vecLru[i] = aux[j];
                }
                vecLru[0] = pgNovo;
        }
        lru.setLru(vecLru);
    }

    public static Bloco[] substituiVetorBuffer(Bloco[] vecBuffer, int posicaoMudanca, Bloco blNovo){
        Bloco[] auxBuffer = vecBuffer;

        vecBuffer[posicaoMudanca] = null;

        vecBuffer[posicaoMudanca] = blNovo;

        return vecBuffer;
    }

    public static Pagina adicionaBlocoBuffer(Bloco blocoNovo, Pagina pagina){
        Bloco[] vecBuffer = buffer.getBuffer().clone();
        //procura espaco livre no buffer
        for(int i = 0; i < vecBuffer.length; i++){
            if(vecBuffer[i] == null){
                vecBuffer[i] = blocoNovo;
                pagina.setPos(i);
                buffer.setBuffer(vecBuffer);
                return pagina;
            }
        }

        //buffer sem espaco livre
        Pagina ultimoLRU = lru.getLru()[lru.getLru().length - 1];

        vecBuffer[ultimoLRU.getPos()] = blocoNovo ;

        pagina.setPos(ultimoLRU.getPos());
        buffer.setBuffer(vecBuffer);
        return pagina;
    }

    public static int pegaPosicaoBuffer (Pagina[] lru, int idBloco, int idCont){

        for (Pagina pagina : lru ) {
            if(pagina == null){
                return 0;
            }

            if (pagina.getFileID() == idCont && pagina.getBlocoID() == idBloco){
                return pagina.getPos();
            }
        }

        return 0;
    }
}
