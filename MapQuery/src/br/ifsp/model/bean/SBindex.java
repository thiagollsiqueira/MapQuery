package br.ifsp.model.bean;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.connection.Pool;
import br.ifsp.model.dao.SbitvectorDAO;
import br.ifsp.model.sbindexcplusplus.SBindexCplusplus;

public class SBindex
{
	private InterfacePool pool;
	
	public int PAGESIZE = 0;
	public int qttyEntries = 0;
	public int SIZE = 0;
		
	private ArrayList<SbitvectorDAO> sbindexes = new ArrayList<SbitvectorDAO>();
	private File filePath;
	
	//private int id;
	private String column_geo;
	private String column_pk;
	private String table;
	
	SBindexCplusplus sbcpp;
	
	public SBindex (File path, int pagesize, int id, String column_geo, String column_pk, String table)
	{
		sbcpp = new SBindexCplusplus();
		
		this.filePath = path;
		this.PAGESIZE = pagesize;
		setQttyEntries(); 
		
		//this.id = id;
		this.column_geo = column_geo;
		this.column_pk = column_pk;
		this.table = table;
		
	}
	
	private void setQttyEntries()
	{
		this.qttyEntries = this.sbcpp.sizeSbitvector(this.PAGESIZE);
	}
	
	/*private int getId()
	{
		return this.id;
	}*/
	
	private String getColumnGeo()
	{
		return this.column_geo;
	}
	
	private String getColumnPk()
	{
		return this.column_pk;
	}
	
	private String getTable()
	{
		return this.table;
	}
		
	public ArrayList<SbitvectorDAO> getArrayList()
	{
		return this.sbindexes;
	}
	
	public void build() throws Exception
	{	
		System.out.println("-----build");
		this.pool = new Pool();
		Connection con = pool.getConnection();
		
		Statement statement = null; 
		ResultSet resultSet = null;
		
		String sql = null;
		
		try
		{
			sql = "SELECT " + this.getColumnPk() + ", ST_XMin(" + this.getColumnGeo() + "), ST_YMin(" + this.getColumnGeo() + "), ST_XMax(" + this.getColumnGeo() + "), ST_YMax(" + this.getColumnGeo() + ") FROM " + this.getTable();
			
			System.out.println(sql);
			
			statement = con.createStatement();
			resultSet = statement.executeQuery(sql);
			
			//calcular valor de 'qttyEntries' --> set.qttyEntries(this.PAGESIZE);
						
			int[] vectKey = new int[this.qttyEntries]; //tamanho do vetor: 'this.qttyEntries'
			double[] vectXMin = new double[this.qttyEntries];
			double[] vectYMin = new double[this.qttyEntries];
			double[] vectXMax = new double[this.qttyEntries];
			double[] vectYMax = new double[this.qttyEntries];			
			
			//instanciar o arquivo
			this.sbcpp.openForCreation(this.filePath.getAbsolutePath(), this.PAGESIZE); 
			//System.out.println(this.filePath.getAbsolutePath());
			
			int cont=0;
			while (resultSet.next())
			{
				if(cont == this.qttyEntries) 
				{
					//chamar para gravar no arquivo - chamando o .c
					this.sbcpp.write(vectKey, vectXMin, vectYMin, vectXMax, vectYMax, vectKey.length);
					
					vectKey = new int[cont]; 
					vectXMin = new double[cont];
					vectYMin = new double[cont];
					vectXMax = new double[cont];
					vectYMax = new double[cont];
					cont = 0;
					
				}
				
				//ler a tabela e obter pk, xMin, yMin, xMax, yMax - gravar o conteúdo em 5 vetores
				vectKey[cont] = resultSet.getInt(1);
				vectXMin[cont] = resultSet.getDouble(2);
				vectYMin[cont] = resultSet.getDouble(3);
				vectXMax[cont] = resultSet.getDouble(4);
				vectYMax[cont] = resultSet.getDouble(5);
				
				cont++;
			}
			
			//gravar o restante - chamando o .c
			this.sbcpp.write(vectKey, vectXMin, vectYMin, vectXMax, vectYMax, cont);
			
			this.sbcpp.closeAfterCreation();
						
		}
		catch (Exception ioException)
		{
			throw new Exception ("Erro no banco de dados");
		} 
		finally
		{
			resultSet.close();
			statement.close();
			pool.liberarConnection(con);
			System.out.println("------fim");
		}  
		
	}
	
	public void takeSBindex()
	{
		
	}
	
	//chamar métodos do C++ (scan_irq, scan_crq, scan_erq) --> tem que ser retornado um vetor de números inteiros, que são os candidatos
	public String /*int*/ query (SpatialQueryWindow sqw ) throws Exception
	{	
		//System.out.println("query");
		String sql = "";
		if(sqw.getPredicateType().equals(QUERY_TYPE.IRQ))
		{
			sqw.setCandidates(this.sbcpp.scanIrq(this.filePath.getAbsolutePath(), sqw.getQueryWindow())); 
						
			if (sqw.getCandidates().length < 1)
			{
				System.out.println("Como não existem candidatos, não é possível fazer consultas.\n");
				sql = "";
			}
			else
			{
				sql = queryDatabaseIrq(sqw);
			}
			 
		}
		else if(sqw.getPredicateType().equals(QUERY_TYPE.CRQ))
		{
			sqw.setCandidates(this.sbcpp.scanCrq(this.filePath.getAbsolutePath(), sqw.getQueryWindow())); 
			
			if (sqw.getCandidates().length < 1)
			{
				System.out.println("Como não existem candidatos, não é possível fazer consultas.\n");
				sql = "";
			}
			else
			{
				sql = queryDatabaseCrq(sqw);
			}
		}
		else if(sqw.getPredicateType().equals(QUERY_TYPE.ERQ))
		{
			sqw.setCandidates(this.sbcpp.scanErq(this.filePath.getAbsolutePath(), sqw.getQueryWindow())); 
			
			if (sqw.getCandidates().length < 1)
			{
				System.out.println("Como não existem candidatos, não é possível fazer consultas.\n");
				sql = "";
			}
			else
			{
				sql = queryDatabaseErq(sqw);
			}
		}
		
		return sql;
	}
	
	//refinamento
	private String queryDatabaseIrq(SpatialQueryWindow sqw) throws Exception
	{
		int size = sqw.getCandidates().length;
		String sql = " ";
		//String or = "";
		
		try
		{
			
			sql = "SELECT " + sqw.getConventionalLevel() +" FROM "+ sqw.getTable() + " WHERE ("+ sqw.getConventionalLevel() + " =  " + sqw.getCandidates()[0];
			
			for (int x = 1; x < size; x++)
			{
				sql += " OR "+ sqw.getConventionalLevel() + " = " + sqw.getCandidates()[x];
				
			}
			
			sql += ") AND ST_Intersects(GEOMFROMTEXT('POLYGON((" + sqw.getQueryWindow()[0] + " " + sqw.getQueryWindow()[1]  + ", " + sqw.getQueryWindow()[0]+ " " + sqw.getQueryWindow()[3] + ", "+
					sqw.getQueryWindow()[2]  + " " + sqw.getQueryWindow()[3] +", " + sqw.getQueryWindow()[2] + " "+ sqw.getQueryWindow()[1] + ", "+ sqw.getQueryWindow()[0] + " " + sqw.getQueryWindow()[1]+ "))', -1), "+ sqw.getSpatialLevel() + ")" +
						" ORDER BY "+ sqw.getConventionalLevel();
			//ST_Intersects(geometry, geometry)
			
			//System.out.println(sql);
		
		} 
		catch (Exception ioException)
		{
			//throw new Exception ("Erro no banco de dados");
			ioException.printStackTrace();
		} 
		
		return sql;
		//return or;
	}
		
	//não precisa de refinamento --> todos os candidatos são respostas
	private String queryDatabaseCrq(SpatialQueryWindow sqw) throws Exception
	{		
		//System.out.println("crq");
		
		int size = sqw.getCandidates().length;
		
		//String or = "";
						
		sqw.setConventionalPredicate("SELECT "+sqw.getConventionalLevel()
				+" FROM "+sqw.getTable()+" WHERE " + sqw.getConventionalLevel() + " = " + sqw.getCandidates()[0]);
				
		for (int x=1; x<size; x++)
		{
			sqw.setConventionalPredicate(" OR "+ sqw.getConventionalLevel() + " = " + sqw.getCandidates()[x]);
		}
				
		return sqw.getConventionalPredicate();
		//return or;
	}
	
	//linestring
	private String queryDatabaseErq(SpatialQueryWindow sqw) throws Exception
	{
		//System.out.println("erq");
		String sql = " ";
		
		int size = sqw.getCandidates().length;
		
		try
		{
			sql = "SELECT " + sqw.getConventionalLevel() + " FROM "+ sqw.getTable() + " WHERE ("+ sqw.getConventionalLevel() + " =  " + sqw.getCandidates()[0];
			
			for (int x=1; x<size; x++)
			{
				sql += " OR "+ sqw.getConventionalLevel() + " = " + sqw.getCandidates()[x]; 
			}
			
			sql += ") AND ST_Within((GEOMFROMTEXT('LINESTRING("+  sqw.getQueryWindow()[0] + " " + sqw.getQueryWindow()[1]  + ", " + sqw.getQueryWindow()[0] + " " + sqw.getQueryWindow()[3] + ", "
				+ sqw.getQueryWindow()[2]  + " " + sqw.getQueryWindow()[3] + ", " + sqw.getQueryWindow()[2] + " " + sqw.getQueryWindow()[1] + ", " + sqw.getQueryWindow()[0] + " " + 
				sqw.getQueryWindow()[1] + ")', -1)), " + sqw.getSpatialLevel() + ") ORDER BY " + sqw.getConventionalLevel();
			//ST_Within(geometry A, geometry B)
			
			//System.out.println(sql);
		}
		catch (Exception ioException)
		{
			throw new Exception ("Erro no banco de dados");
		} 
		
		return sql;
		//return or;
	}
	
}