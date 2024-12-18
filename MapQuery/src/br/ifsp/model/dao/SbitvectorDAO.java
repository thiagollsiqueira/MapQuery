package br.ifsp.model.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.ifsp.model.bean.SBindex;
import br.ifsp.model.bean.SpatialQueryWindow;
import br.ifsp.model.connection.InterfacePool;

public class SbitvectorDAO {
	private InterfacePool pool;
	private String table;
	
	public SbitvectorDAO(InterfacePool pool)
	{
		this.pool = pool;
	}

	public String getSbitvectors(SpatialQueryWindow sqw, SBindex sbindex, String sb) throws SQLException {
		
		List<Integer> resultado = new ArrayList<Integer>();
		String sqlSelect = "";
				
		Connection con = pool.getConnection();
		Statement statement = null;
		ResultSet rs = null;
		try 
		{
			//sbindex.build();
			sqlSelect = sbindex.query(sqw);
			
			if(!sqlSelect.isEmpty())
			{
				System.out.println("\nsqlSelect: "+sqlSelect);
				statement = con.createStatement();
				rs = statement.executeQuery(sqlSelect);
				
				resultado = getListaSbitvector(rs);
				
				rs.close();
				statement.close();
			}
			else
				return sqlSelect;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);			
		}
		
		String strFinal = tratarResultado(resultado, sb);
		
		return strFinal;
		/*try 
		{
			//System.out.println("-----antes build");
			//sbindex.build();
			//System.out.println("-----depois build");
			sqlSelect = sbindex.query(sqw);
			System.out.println("-----sqlSelect: "+sqlSelect);
			
			/*if(!sqlSelect.isEmpty())
			{
				System.out.println("\nsqlSelect: "+sqlSelect);
				statement = con.createStatement();
				rs = statement.executeQuery(sqlSelect);
				
				resultado = getListaSbitvector(rs);
				
				rs.close();
				statement.close();
			}
			else
				return sqlSelect;
			return sqlSelect;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);			
		}
		
		//String strFinal = tratarResultado(resultado, sb);
		
		//return strFinal;
		return null;*/
	}
	
	private List<Integer> getListaSbitvector(ResultSet rs) 
	{
		List<Integer> resultado = new ArrayList<Integer>();
		
		try {
			
			while(rs.next())
			{
				resultado.add(rs.getInt(1));
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return resultado;
	}
	
	
	private String tratarResultado(List<Integer> resultado, String sbindex) 
	{
		String resultadoTemp;
		String resultadoFinal;
		String or = "";
		
		if(resultado.size()>0)
		{
			or += sbindex + " = " + resultado.get(0);
			
			for (int x=1; x<resultado.size(); x++)
			{
				or += " OR "+ sbindex + " = " + resultado.get(x); 
			}
			
			resultadoTemp = or.replace("[", "");
			resultadoFinal = resultadoTemp.replace("]", "");
			
			return resultadoFinal;
		}
		else
			return or;
	}


	public void setTable(String tableName) 
	{
		this.table = tableName;
	}
	
	public String getTable()
	{
		return this.table;
	}


}
