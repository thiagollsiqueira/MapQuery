package br.ifsp.model.bean;

public class SpatialQueryWindow 
{
	/*queryWindow --> é um vetor de double que define o MBR da janela de consulta (xMin, yMin, xMax e yMax)
	 spatialLevel --> define sobre qual atributo do banco de dados deve incidir a consulta espacial (por exemplo, city_geo)
	 conventionalLevel --> é o atributo chave da tabela de dimensão espacial (por exemplo, city_pk)
	 predicateType --> define se o predicado espacial que deve ser avaliado pela janela de consulta é PQ, IRQ, CRQ ou ERQ*/
		
	private double[] queryWindow;
	private String spatialLevel;
	private String conventionalLevel;
	private QUERY_TYPE predicateType;
	private int[] candidates;
	private String conventionalPredicate;
	private String table;
		
	public SpatialQueryWindow (double[] queryWindow, String tableQuery, String spatialLevel, String conventionalLevel, QUERY_TYPE predicateType )
	{
		 this.queryWindow = queryWindow;
		
		 this.spatialLevel = spatialLevel;
		 
		 this.conventionalLevel = conventionalLevel;
		 
		 this.predicateType = predicateType;
		 
		 this.conventionalPredicate = "";
		 
		 this.table = tableQuery;
	}
	
	public double[] getQueryWindow()
	{
		return this.queryWindow;
	}
	
	
	public String getSpatialLevel()
	{
		return this.spatialLevel;
	}

	
	public String getConventionalLevel()
	{
		return this.conventionalLevel;
	}
	
	public String getTable()
	{
		return this.table;
	}
	
	public QUERY_TYPE getPredicateType()
	{
		return this.predicateType;
	}
	
	
	public void setCandidates(int[] arrayCandidates)
	{
		this.candidates = arrayCandidates;
	}
	public int[] getCandidates()
	{
		return this.candidates;
	}
	
	
	public void setConventionalPredicate(String conventPredicate)
	{
		this.conventionalPredicate += conventPredicate;
	}
	public String getConventionalPredicate()
	{
		return this.conventionalPredicate;
	}
	
}