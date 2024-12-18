package br.ifsp.model.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import br.ifsp.model.connection.InterfacePool;


public class MapaDAO {
	private InterfacePool pool;
	
	public MapaDAO(InterfacePool pool)
	{
		this.pool = pool;
	}
	
	public ArrayList<String> getMap(String tabela) throws Exception
	{
		Connection con = pool.getConnection();
		Statement statement = null;
		ResultSet rs = null;
		ArrayList<String> geoms = new ArrayList<String>();
		//select region_geo from region where ST_NumGeometries(region_geo) = 1

		String sql = "SELECT st_astext("+tabela+"_geo) FROM "+tabela+" WHERE ST_NumGeometries("+tabela+"_geo) = 1 ";//AND "+tabela+"_pk = "+id;
				
		try 
		{
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			
			while (rs.next())
			{
				geoms.add(rs.getString(1));
			}
			
			return geoms;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);		
			statement.close();
		}

		return null;
	}
}
