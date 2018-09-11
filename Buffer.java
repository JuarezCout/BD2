public class Buffer {
    private static Bloco[] lru =  new Bloco[40];

    private static Bloco[] buffer =  new Bloco[40];

    private static int[] cacheHitMiss = new int[3];

    private static int idCall, idBuff, controle;

    public static int[] executaBuffer (Container container){

        for (Bloco blocoCall : container.blocos)
            for (Bloco blocoBuff : buffer) {
                if(blocoBuff == null){
                    buffer[controle] = blocoCall;
                }
                cacheHitMiss[2]++;
                idCall = Bloco.byteToInt(Bloco.getBytes(blocoCall.dados, 1, 3));
                idBuff = Bloco.byteToInt(Bloco.getBytes(blocoBuff.dados, 1, 3));
                if (idCall == idBuff) {
                    //Implementar LRU
                } else {

                }

                controle++;
            }

        return cacheHitMiss;
    }
}
