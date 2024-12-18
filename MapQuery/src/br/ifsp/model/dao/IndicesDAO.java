package br.ifsp.model.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.ifsp.model.bean.Tabela;
import br.ifsp.model.connection.InterfacePool;

public class IndicesDAO {
	
	private InterfacePool pool;
	
	
	public IndicesDAO(InterfacePool pool) 
	{
		this.pool = pool;
	}
	
	public void initializeIbj(ArrayList<String> atributos, String nomeIndice, ArrayList<String> geoms)
	{
		Tabela table = new Tabela();
		String colunas = table.from(atributos);
		String constraints = table.pegarConstraints(atributos);
		
		String create = "CREATE TABLE inter." + nomeIndice + " AS (";
		String select = "SELECT ";
		String from = " FROM "+colunas;
		String where = " WHERE "+constraints; 
		String atribFB = "";
		int i = 0;
		
		ArrayList<String> tipos = table.tipos(atributos);
		for(String atributo: atributos)
		{
			//if(atributo.endsWith("_geo"))
			//{
				//não será utilizado
			//}
			//else
			//{
				if(!atribFB.isEmpty())
				{
					atribFB += ",";
				}
				atribFB += atributo; 
				atribFB += ":" + tipos.get(i);
			//}
			
			i++;
		}

		
		if(atribFB.contains("_pk"))
		{
			atribFB = atribFB.replace("_pk", "");
		}
		
		if(atribFB.contains("bpchar"))
		{
			atribFB = atribFB.replace("bpchar", "key");
		}
		if(atribFB.contains("int4"))
		{
			atribFB = atribFB.replaceAll("int4", "int");
		}
		if(atribFB.contains("float8"))
		{
			atribFB = atribFB.replaceAll("float8", "float");
		}
		if(atribFB.contains("int2"))
		{
			atribFB = atribFB.replaceAll("int2", "int");
		}
		if(atribFB.contains("varchar"))
		{
			atribFB = atribFB.replaceAll("varchar", "key");
		}
		
		
		System.out.println(" - atributosFB: "+atribFB);
		
		for(int x=0; x<atributos.size(); x++)
		{			
			if(x>0 && x<atributos.size())
				select += ", ";
			
			select += atributos.get(x);
		}

		System.out.println(" ---------SQL: "+create + select + from + where +")");
		
		
		gravarColunas(nomeIndice, atributos);
		criarTemp(create + select + from + where + ")");
		copyIBJ(nomeIndice);
		inserirIBJ(nomeIndice, geoms);
		iniciarFastBit(nomeIndice, atribFB);
		//CONSIDERAR COLOCAR RETURN TRUE OU FALSE NOS MÉTODOS CHAMADOS----------------------------
	}
	
	private void gravarColunas(String nomeIndice, ArrayList<String> columns)
	{
		
		String colunas = "";
		try 
		{
			for(String column:columns)
			{
				if(column.startsWith("lo_"))
				{
					column = column.replace(column, "sum("+column+")");
					//System.out.println("lo_ -> "+column);
				}
				
				if(column.endsWith("_pk") /*|| column.endsWith("_fk")*/)
				{
					
				}
				else
				{
					if(!colunas.isEmpty())
						colunas += ", ";
					colunas += column;
				}
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\postgres\\Documents\\column_"+nomeIndice+".txt")));
			bw.write(colunas);
			bw.close();
			System.out.println(" - colunas gravadas: "+colunas);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}		
	}
	
	public String lerColunas(String nomeIndice)
	{
		String colunas = "";
		try 
		{
	        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\postgres\\Documents\\column_"+nomeIndice+".txt"));
	        String str;
	        
	        while((str = br.readLine()) != null)
	        {
	           colunas+= str;
	        }
	        
	        System.out.println(" - colunas lidas: "+colunas);
	        
	        br.close();
	    } 
	    catch (IOException e){
	    	e.printStackTrace();
	    }
		
		return colunas;
	}
	
	//cria a tabela temporária
	private void criarTemp(String sqlCreate) 
	{
		System.out.println(" --------criarTemp");
		
		Connection con = pool.getConnection();
		Statement statement = null;
		
		try 
		{
			statement = con.createStatement();
			statement.executeUpdate(sqlCreate);
			statement.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);			
		}	
	}
	
	public void copyIBJ(String nomeIndice)
	{
		Connection con = pool.getConnection();
		Statement statement = null;
		String copy = "copy inter."+ nomeIndice +" to 'C:/Users/postgres/"+nomeIndice+".csv' with delimiter ',' csv quote ''''"; // force quote p_brand1";
		System.out.println(" - "+copy);
		try 
		{
			statement = con.createStatement();
			statement.executeUpdate(copy);
			statement.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);			
		}	
	}
	
	private void inserirIBJ(String nomeIndice, ArrayList<String> geoms)
	{
		Connection con = pool.getConnection();
		Statement statement = null;
		String sqlInsert = "INSERT INTO inter.ibj (nome_indice, geoms) VALUES ('"+nomeIndice+"', '";
		ArrayList<String> geometrias = new ArrayList<String>();
		for(String geom: geoms)
		{
			if(geom.endsWith("_geo"))
				geometrias.add(geom.replace("_geo", ""));
		}
		System.out.println(geometrias);
		for(int i=0; i<geometrias.size(); i++)
		{
			
			if(i>0)
				sqlInsert += ", ";
			sqlInsert += geometrias.get(i);
		}
		sqlInsert +="')";
				
		try 
		{
			statement = con.createStatement();
			statement.executeUpdate(sqlInsert);
			
			statement.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);			
		}
		
	}
	
	public List<String> getAtributos(String tableName)
	{
		List<String> resultado = new ArrayList<String>();
		
		String sqlSelect = "";
		
		Connection con = pool.getConnection();
		Statement statement = null;
		ResultSet rs = null;
				
		try 
		{
			sqlSelect = "select * from inter."+ tableName +" limit 1"; 
			
			statement = con.createStatement();
			rs = statement.executeQuery(sqlSelect);
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int numberColumns = rsmd.getColumnCount();
			
			for(int aux=1; aux<=numberColumns; aux++)
			{
				resultado.add(rsmd.getColumnName(aux));
			}
			
			rs.close();
			statement.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);			
		}
		
		return resultado;
	}
	
	public List<String> getIndices()
	{
		List<String> resultado = new ArrayList<String>();
		
		String sqlSelect = "";
		
		Connection con = pool.getConnection();
		Statement statement = null;
		ResultSet rs = null;
				
		try 
		{
			sqlSelect = "select nome_indice from inter.ibj order by nome_indice"; 
			
			statement = con.createStatement();
			rs = statement.executeQuery(sqlSelect);
			
			while(rs.next())
			{
				resultado.add(rs.getString(1));
			}
				
			rs.close();
			statement.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);			
		}
		
		return resultado;
	}
	
	public List<String> getGeoms(String nomeIndice)
	{
		List<String> resultado = new ArrayList<String>();
		
		String sqlSelect = "";
		
		Connection con = pool.getConnection();
		Statement statement = null;
		ResultSet rs = null;
				
		try 
		{
			sqlSelect = "select geoms from inter.ibj where nome_indice='"+nomeIndice+"'"; 
			
			statement = con.createStatement();
			rs = statement.executeQuery(sqlSelect);
			
			while(rs.next())
			{
				resultado.add(rs.getString(1));
			}
				
			rs.close();
			statement.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally
		{
			pool.liberarConnection(con);			
		}
		
		return resultado;
	}
	

	public void iniciarFastBit(String table, String atribFB)
	{
		String comando = "";
		
		comando = "C:/fastbit-ibis1.3.0/fastbit-ibis1.3.0/win/Release/ardea.exe -d C:/fastbit-ibis1.3.0/fastbit-ibis1.3.0/win/Release/"+table+"/ -m \""+atribFB+"\" -t C:/Users/postgres/"+table+".csv";  
		//System.out.println(comando);
		
		try
		{
			String line;
			Process p = Runtime.getRuntime().exec(comando); 
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = input.readLine()) != null) 
			{
				System.out.println(line);
			}
			input.close();
			
			while ((line = bre.readLine()) != null) 
			{
				System.out.println(line);
			}
			bre.close();
			
			p.waitFor();
			System.out.println(" ---------First step done.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
		criarIBJ(table);
	}
	
	private void criarIBJ(String table)
	{
		try
		{
			String comando = "C:/fastbit-ibis1.3.0/fastbit-ibis1.3.0/win/Release/ibis.exe -d C:/fastbit-ibis1.3.0/fastbit-ibis1.3.0/win/Release/"+table+" -b \"<binning none/><encoding equality/>\"";  
			//System.out.println(comando);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("ibis.bat")));
			bw.write(comando);
			bw.close();
			
			String line;
			Process p = Runtime.getRuntime().exec(new File("ibis.bat").getAbsolutePath()); 
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) 
			{
				System.out.println(line);
			}
			input.close();

			System.out.println(" ---------Ibj's creation done.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
		
	}

	public List<String> consultarFastBit(String select, String where, String table) 
	{
		String nomeArq = "C:/fastbit-ibis1.3.0/fastbit-ibis1.3.0/win/Release/"+table+"/q.txt";
		
		List<String> resultado = new ArrayList<String>();
		//String orderBy = "";
				
		//RETIRAR ESSA PARTE - TESTAR gravarColuna()
		//if(select.contains("lo_revenue"))
		//{
			//select = select.replace("lo_revenue", "sum(lo_revenue)");;
		//}
		
		if(!where.isEmpty())
		{
			try
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File("ibis.bat")));
				String comando = "C:/fastbit-ibis1.3.0/fastbit-ibis1.3.0/win/Release/ibis.exe -d C:/fastbit-ibis1.3.0/fastbit-ibis1.3.0/win/Release/"+table+" -q \""+select +" "+where+"\" -o "+nomeArq;  
				//System.out.println(comando);
				bw.write(comando); 
				bw.close();
				
				String line;
				Process p = Runtime.getRuntime().exec(new File("ibis.bat").getAbsolutePath()); 
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) 
				{
					System.out.println(line);
				}
				input.close();
				
				System.out.println(" ---------Query done.");
				 
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			resultado = lerResultado(nomeArq);
			
		}
		return resultado;
	}
	
	private List<String> lerResultado(String arquivo)
	{
		List<String> resultados = new ArrayList<String>();
				
		try 
		{
	        BufferedReader in = new BufferedReader(new FileReader(arquivo));
	        String str;
	        
	        while((str = in.readLine()) != null)
	        {
	        	System.out.println(str);
		           resultados.add(str);
	        }
	        
	        in.close();
	    } 
	    catch (IOException e){
	    	e.printStackTrace();
	    }
		List<String> result = new ArrayList<String>();
		
		for(int r=0; r<resultados.size(); r++)
		{
			if(resultados.get(r).contains("\""))
			{
				result.add(resultados.get(r).replace("\"", "\'"));
			}
			else
				result.add(resultados.get(r));
		}
		
		return result;
	}
	
}
