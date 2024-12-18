package br.ifsp.model.sbindexcplusplus;

public class SBindexCplusplus 
{	
	/* Abre um arquivo, a partir do caminho passado por par�metro, para leitura e escrita. 
	 * Faz aloca��o din�mica de mem�ria para a p�gina e para o buffer e coloca o ponteiro do arquivo depois da posi��o 'head'.
	 * Lembrando que 'head' � igual a 4096 bytes, precedendo os vetores no arquivo.*/
	public native void openForCreation(String path, int pagesize);
	
	/* L� os valores dos vetores nas vari�veis correspondentes, gravando-as em uma posi��o do vetor 'buffer'.
	 * Copia o conte�do de 'buffer' para a p�gina e escreve a p�gina no arquivo.*/
	public native void write(int key[], double xMin[], double yMin[], double xMax[], double yMax[], int length);
	
	/* Libera a mem�ria anteriormente alocada.
	 * Escreve 'cardinality' no 'head' do arquivo, assim como escreve o arquivo e o fecha. */
	public native void closeAfterCreation();
	
	public native int[] scanIrq(String path, double[] queryWindow/*, double xMin, double yMin, double xMax, double yMax*/);
	
	public native int[] scanCrq(String path, double[] queryWindow/*, double xMin, double yMin, double xMax, double yMax*/);
	
	public native int[] scanErq(String path, double[] queryWindow/*, double xMin, double yMin, double xMax, double yMax*/);
	
	public native int[] scanPq(String path, double[] queryWindow);
	
	/*No Java, o n�mero de bytes de Sbitvector � 36, sendo que a linguagem Java n�o tem sizeof(), portanto as vari�veis t�m n�mero constante de bytes.
	 * Por�m em C/C++, usamos o sizeof() para calcular o tamanho de Sbitvector.
	 * Os tamanhos acabam sendo diferentes, mas temos que usar o mesmo valor em Java e C++ para calcularmos corretamente as vari�veis.
	 * Dessa forma, criamos um m�todo em C++ que calcular que n�mero de bytes necess�rios, retornando-os. O n�mero retornado � utilizado no c�culo de vari�veis no c�digo Java.*/
	public native int sizeSbitvector(int pagesize);
	
	public native void SpatialQueryWindow(double[] qWindow);
}
