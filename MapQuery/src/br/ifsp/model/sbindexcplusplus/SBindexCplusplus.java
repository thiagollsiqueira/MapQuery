package br.ifsp.model.sbindexcplusplus;

public class SBindexCplusplus 
{	
	/* Abre um arquivo, a partir do caminho passado por parâmetro, para leitura e escrita. 
	 * Faz alocação dinâmica de memória para a página e para o buffer e coloca o ponteiro do arquivo depois da posição 'head'.
	 * Lembrando que 'head' é igual a 4096 bytes, precedendo os vetores no arquivo.*/
	public native void openForCreation(String path, int pagesize);
	
	/* Lê os valores dos vetores nas variáveis correspondentes, gravando-as em uma posição do vetor 'buffer'.
	 * Copia o conteúdo de 'buffer' para a página e escreve a página no arquivo.*/
	public native void write(int key[], double xMin[], double yMin[], double xMax[], double yMax[], int length);
	
	/* Libera a memória anteriormente alocada.
	 * Escreve 'cardinality' no 'head' do arquivo, assim como escreve o arquivo e o fecha. */
	public native void closeAfterCreation();
	
	public native int[] scanIrq(String path, double[] queryWindow/*, double xMin, double yMin, double xMax, double yMax*/);
	
	public native int[] scanCrq(String path, double[] queryWindow/*, double xMin, double yMin, double xMax, double yMax*/);
	
	public native int[] scanErq(String path, double[] queryWindow/*, double xMin, double yMin, double xMax, double yMax*/);
	
	public native int[] scanPq(String path, double[] queryWindow);
	
	/*No Java, o número de bytes de Sbitvector é 36, sendo que a linguagem Java não tem sizeof(), portanto as variáveis têm número constante de bytes.
	 * Porém em C/C++, usamos o sizeof() para calcular o tamanho de Sbitvector.
	 * Os tamanhos acabam sendo diferentes, mas temos que usar o mesmo valor em Java e C++ para calcularmos corretamente as variáveis.
	 * Dessa forma, criamos um método em C++ que calcular que número de bytes necessários, retornando-os. O número retornado é utilizado no cáculo de variáveis no código Java.*/
	public native int sizeSbitvector(int pagesize);
	
	public native void SpatialQueryWindow(double[] qWindow);
}
