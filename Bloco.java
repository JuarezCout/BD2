import java.nio.ByteBuffer;

public class Bloco {
	byte[] dados = new byte[2048];

	Bloco(String[] linha) {
		byte[] aux = new byte[2048];



		this.dados = aux;
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
}
