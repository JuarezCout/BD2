import java.nio.ByteBuffer;

public class Bloco {
	int tamanho = 2048;
	byte[] dados;

	//cria bloco normal
	Bloco(int idBloco, byte idContainer) {
		byte[] aux = new byte[tamanho];

		aux[0] = idContainer; //id do container
		aux = bytePlusbyte(aux, intToByte(idBloco), 1); //id do bloco
		aux[4] = 0; //status do container
		aux = bytePlusbyte(aux, intToByte(8), 5); //tamanho do bloco

		this.dados = aux;
	}
	//cria bloco controle
	Bloco(String linha) {
		byte[] aux = new byte[tamanho];

		aux[0] = 1;//id do container
		aux = bytePlusbyte(aux, intToByte(tamanho), 1);//tamanho do bloco
		aux[4] = 0; //status do container
		aux = bytePlusbyte(aux, intToByte(0),5); //id do proximo bloco livre

		byte[] linhaByte = linha.getBytes(); //grava linha de byte
		aux = bytePlusbyte(aux, intTo2Byte(linhaByte.length), 9);
		aux = bytePlusbyte(aux, linhaByte, 11);

		this.dados = aux;
	}

	void adicionarTuplaNoBloco(byte[] tupla) {
		int bytesUsados = byteToInt(getBytes(this.dados, 5, 3));
		this.dados = bytePlusbyte(this.dados, tupla, bytesUsados);

		setBytes(intToByte(bytesUsados + tupla.length), 5, 3);
	}

	int getTamanhoBloco() {
		return byteToInt(getBytes(dados, 5, 3));
	}
	//retorna conjunto de bytes de um array de bytes
	static byte[] getBytes(byte[] dadosRecebidos, int posicaoInicial, int deslocamento){
		byte[] bytes = new byte[deslocamento];

		for(int i = posicaoInicial, j = 0; i < posicaoInicial + deslocamento; i++, j++){
			bytes[j] = dadosRecebidos[i];
		}

		return bytes;
	}
	//seta bytes em posicoes determinadas de um array de bytes
	void setBytes(byte[] dadosRecebidos, int posicaoInicial, int deslocamento){
		for(int i = posicaoInicial, j = 0; i < posicaoInicial + deslocamento; i++, j++){
			dados[i] = dadosRecebidos[j];
		}
	}
	//concatena array de bytes
	static byte[] bytePlusbyte(byte[] valor1, byte[] valor2, int posicao){
		for(int i = posicao, j = 0; i < valor2.length + posicao; i++, j++){
			valor1[i] = valor2[j];
		}
		return valor1;
	}

	static int byteToInt(byte[] bytes) {
		byte[] result = new byte[4];

		result[0] = 0;
		result[1] = bytes[0];
		result[2] = bytes[1];
		result[3] = bytes[2];

		ByteBuffer buf = ByteBuffer.wrap(result);

		return buf.getInt();
	}

	static byte[] intToByte(int i) {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(i);

		byte[] bytes = buf.array();

		byte[] result = new byte[3];
		result[0] = bytes[1];
		result[1] = bytes[2];
		result[2] = bytes[3];

		return result;
	}

	static byte[] intTo2Byte(int i) {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(i);

		byte[] bytes = buf.array();

		byte[] result = new byte[2];
		result[0] = bytes[2];
		result[1] = bytes[3];

		return result;
	}

	static int byte2ToInt(byte[] bytes) {
		byte[] result = new byte[4];

		result[0] = 0;
		result[1] = 0;
		result[2] = bytes[0];
		result[3] = bytes[1];

		ByteBuffer buf = ByteBuffer.wrap(result);

		return buf.getInt();
	}

	static String byteToString(byte[] bytes){

	    return new String(bytes);

    }
}
