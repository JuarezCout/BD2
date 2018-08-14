package estruturas;

import java.nio.ByteBuffer;
import java.util.Random;

public class Bloco {
	
	public static final int tamanho = 2048;
	public static int idContainer = 0;
	public int idBloco;
	public int tipoBloco;
	public int byteUsado;
	public byte[] dados;
	
	Random gerador = new Random();
	byte[] bloco;

	public Bloco(byte[] bloco) {
		super();
		idBloco = gerador.nextInt(999);
		tipoBloco = 0;
	    byteUsado = 8;
	    dados = new byte[tamanho];

	    //ID do Container(Arquivo de Dados)
	    dados[0] = (byte) idContainer;
	    //ID do Bloco
	    byte[] bytes = ByteBuffer.allocate(3).putInt(idBloco).array();
	    int i = 1;
	    int j = 0;
	    while (i < 4) {
	    	dados[i] = bytes[j];
	    	i++; j++;
		}
	    //Tipo do Bloco
	    dados[4] = (byte) 0; //Sem uso
	    //Controle de quais bytes estão em uso / Próximo Byte livre
	    bytes = ByteBuffer.allocate(3).putInt(byteUsado).array();
	    i = 5;
	    j = 0;
	    while (i < 8) {
	    	dados[i] = bytes[j];
	    	i++; j++;
		}
	}
	
	

}
