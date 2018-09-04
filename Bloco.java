import java.nio.ByteBuffer;

public class Bloco {
	int tamanho = 2048;
	byte[] dados;

	Bloco(String linha) {
		byte[] aux = new byte[tamanho];

		aux[0] = 1;
		aux = bytePlusbyte(aux, intToByte(tamanho), 1);
		aux[4] = 0;
		aux = bytePlusbyte(aux, intToByte(0),5);

		byte[] linhaByte = linha.getBytes();
		aux = bytePlusbyte(aux, intTo2Byte(linhaByte.length), 9);
		aux = bytePlusbyte(aux, linhaByte, 11);

		this.dados = aux;
	}

	byte[] bytePlusbyte(byte[] valor1, byte[] valor2, int posicao){
		for(int i = posicao, j = 0; i < i + valor2.length; i++, j++){
			valor1[i] = valor2[j];
		}
		return valor1;
	}

	int byteToInt(byte[] bytes) {
		byte[] result = new byte[4];

		result[0] = 0;
		result[1] = bytes[0];
		result[2] = bytes[1];
		result[3] = bytes[2];

		ByteBuffer buf = ByteBuffer.wrap(result);

		return buf.getInt();
	}

	byte[] intToByte(int i) {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(i);

		byte[] bytes = buf.array();

		byte[] result = new byte[3];
		result[0] = bytes[1];
		result[1] = bytes[2];
		result[2] = bytes[3];

		return result;
	}

	byte[] intTo2Byte(int i) {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(i);

		byte[] bytes = buf.array();

		byte[] result = new byte[2];
		result[0] = bytes[2];
		result[1] = bytes[3];

		return result;
	}
}
