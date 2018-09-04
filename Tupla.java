

public class Tupla {

	byte[] dados;

	byte[] montaTuplaByte(String[] linha){
		byte[] tupla = new byte[4];

		for(int i = 0; i < linha.length; i++) {
			byte[] linhaByte = linha[i].getBytes();
			byte[] coluna = new byte[2 + linhaByte.length];
			byte[] tamanhoColuna = Bloco.intTo2Byte(linhaByte.length);

			coluna = Bloco.bytePlusbyte(coluna, tamanhoColuna, 0);
			coluna = Bloco.bytePlusbyte(coluna, linhaByte, 2);

			byte[] novaTupla = new byte[tupla.length + coluna.length];
			novaTupla = Bloco.bytePlusbyte(novaTupla, Bloco.intToByte(somaTotalBytes(tupla, coluna)), 0);
			tupla = Bloco.bytePlusbyte(novaTupla, coluna, tupla.length);
		}

		return tupla;

	}

	int somaTotalBytes(byte[] tupla, byte[] coluna) {
		int totalTupla = Bloco.byteToInt(Bloco.getBytes(tupla,0, 4));
		int totalColuna = Bloco.byte2ToInt(Bloco.getBytes(coluna,0, 2));

		return totalTupla + totalColuna;
	}


}
