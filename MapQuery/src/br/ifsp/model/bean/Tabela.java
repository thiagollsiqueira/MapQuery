package br.ifsp.model.bean;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.connection.Pool;

public class Tabela {
		
	public ArrayList<String> colunasEspaciais()
	{
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		ArrayList<String> result = new ArrayList<String>();
		String tabela, coluna;
		
		ResultSet rsTables;
		ResultSet rsColumns;
		
		try 
		{
			DatabaseMetaData md = con.getMetaData();
			String[] types = {"TABLE"};
			rsTables = md.getTables(null, "public", "%", types);
						
			while(rsTables.next())
			{
				tabela = rsTables.getString(3); //3 == tabela
				rsColumns = md.getColumns(null, "public", tabela, "%");
				//System.out.println(tabela);
				
				while(rsColumns.next())
				{
					coluna = rsColumns.getString(4); //4 == coluna
					
					if(rsColumns.getString(6).equals("geometry")) //6 == tipo da coluna
						result.add(coluna);
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return result;
	}

	public ArrayList<String> colunasConvencionais()
	{
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		ArrayList<String> result = new ArrayList<String>();
		//ArrayList<String> pks = new ArrayList<String>();
		String tabela, coluna; //, pk;
		
		ResultSet rsTables;
		ResultSet rsColumns;
		//ResultSet rsExpConstraints;
		
		try 
		{
			DatabaseMetaData md = con.getMetaData();
			String[] types = {"TABLE"};
			rsTables = md.getTables(null, "public", "%", types);
						
			while(rsTables.next())
			{
				tabela = rsTables.getString(3); //3 == tabela
				//System.out.println(tabela);
				if(tabela.equals("geometry_columns")){
				}
				else if(tabela.equals("spatial_ref_sys")){
				}
				else{
				//verifica as chaves primárias
				/*rsExpConstraints = md.getExportedKeys(null, "public", tabela);
				while(rsExpConstraints.next())
				{
					pk = rsExpConstraints.getString(4);
					//System.out.println("pk: "+pk);
					pks.add(pk);
				}*/
				
					//verifica as colunas
					rsColumns = md.getColumns(null, "public", tabela, "%");
					while(rsColumns.next())
					{
						coluna = rsColumns.getString(4); //4 == coluna
						if(coluna.endsWith("_pk"))
						{
							//System.out.println(coluna);
							rsColumns.next(); //passa para a próxima coluna
							coluna = rsColumns.getString(4); //4 == coluna //atualiza a variável
							
							if(rsColumns.getString(6).equals("geometry")) //6 == tipo da coluna
							{
								//System.out.println(coluna);
							}
							else
								result.add(coluna);
						}
						else
							result.add(coluna);
					}
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return result;
	}
	
	/*public ArrayList<String> pegarColunas()
	{
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		ArrayList<String> result = new ArrayList<String>();
		String tabela, coluna;
		
		ResultSet rsTables;
		ResultSet rsColumns;
		//ResultSet rsConstraints;
		
		try 
		{
			DatabaseMetaData md = con.getMetaData();
			String[] types = {"TABLE"};
			rsTables = md.getTables(null, "public", "%", types);
						
			while(rsTables.next())
			{
				tabela = rsTables.getString(3); //3 == tabela
				rsColumns = md.getColumns(null, "public", tabela, "%");
				//System.out.println(tabela);
				
				while(rsColumns.next())
				{
					coluna = rsColumns.getString(4); //4 == coluna
					if(tabela.equals("geometry_columns")) //não é uma opção para o usuário
					{
						//System.out.print("-");
					}
					else if(tabela.equals("spatial_ref_sys")) //não é uma opção para o usuário
					{
						//System.out.print("-");
					}
					else
						result.add(coluna);
					
					//System.out.println("    "+coluna);
					//System.out.println("tipo: "+rsColumns.getString(6)); //6 == tipo da coluna
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return result;
	}*/
	
	public String from(ArrayList<String> columns)
	{
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		String tabela, coluna;
		
		ResultSet rsTables;
		ResultSet rsColumns;

		String from = "";
		
		try 
		{
			DatabaseMetaData md = con.getMetaData();
			String[] types = {"TABLE"};
			for(String column: columns)
			{
				rsTables = md.getTables(null, "public", "%", types);
			
				//System.out.println("column: "+column);
				while(rsTables.next())
				{
					tabela = rsTables.getString(3); //3 == tabela
					rsColumns = md.getColumns(null, "public", tabela, "%");
					
					while(rsColumns.next())
					{
						coluna = rsColumns.getString(4); //4 == coluna
						
						if(column.equals(coluna))
						{
							//System.out.println("coluna inserida: "+coluna);
							if(!from.contains(tabela))
							{
								if(from.isEmpty())
									from += tabela;
								else
									from += ", "+tabela;
							}
							//System.out.println("tabela: "+tabela);
						}
					}
				}
			}
			System.out.println(" - from: "+from);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return from;
	}
	
	
	public ArrayList<String> tipos(ArrayList<String> columns)
	{
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		String tabela;
		String coluna;
		
		ResultSet rsTables;
		ResultSet rsColumns;

		ArrayList<String> tipos = new ArrayList<String>(); 
		
		try 
		{
			DatabaseMetaData md = con.getMetaData();
			String[] types = {"TABLE"};
			for(String column: columns)
			{
				rsTables = md.getTables(null, "public", "%", types);
			
				while(rsTables.next())
				{
					tabela = rsTables.getString(3); //3 == tabela
					rsColumns = md.getColumns(null, "public", tabela, "%");
					
					while(rsColumns.next())
					{
						coluna = rsColumns.getString(4); //4 == coluna
						
						if(column.equals(coluna))
						{
							tipos.add(rsColumns.getString(6)); //6 == tipo
						}
					}
				}
			}
			//System.out.println("tipos: "+tipos);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return tipos;
	}
	
	public String pegarConstraints(ArrayList<String> columns)
	{
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		String tabela, coluna = "", pk, fk, from="";
		String whereFinal="";
		ArrayList<String> whereExp = new ArrayList<String>();
		ArrayList<String> whereImp = new ArrayList<String>();
		
		ResultSet rsTables;
		ResultSet rsColumns;
		ResultSet rsImpConstraints;
		ResultSet rsExpConstraints;
		
		try 
		{
			DatabaseMetaData md = con.getMetaData();
			String[] types = {"TABLE"};
			rsTables = md.getTables(null, "public", "%", types);
						
			for(String column: columns)
			{
				rsTables = md.getTables(null, "public", "%", types);
			
				while(rsTables.next())
				{
					tabela = rsTables.getString(3); //3 == tabela
					rsColumns = md.getColumns(null, "public", tabela, "%");
					
					while(rsColumns.next())
					{
						coluna = rsColumns.getString(4); //4 == coluna
						
						if(column.equals(coluna))
						{
							if(!from.contains(tabela))
							{
								if(from.isEmpty())
									from += tabela;
								else
									from += ", "+tabela;
							}
							//System.out.println("tabela: "+tabela);
							
							rsExpConstraints = md.getExportedKeys(null, "public", tabela);
							
							while(rsExpConstraints.next())
							{
								//System.out.println("   exp");
								pk = rsExpConstraints.getString(4);
								fk = rsExpConstraints.getString(8); 
								//System.out.println("    -> "+ pk + " = " + fk);
								whereExp.add(pk + " = " + fk);
								
							}
							
							rsImpConstraints = md.getImportedKeys(null, "public", tabela);
							
							while(rsImpConstraints.next())
							{
								//System.out.println("   imp");
								pk = rsImpConstraints.getString(4);
								fk = rsImpConstraints.getString(8); 
								//System.out.println("    -> "+ pk + " = " + fk);
								whereImp.add(pk + " = " + fk);
							}
							
							/*se as comprações entre as constraints 'exported' e 'imported' 
							 * são iguais, significa que elas são necessárias na cláusula where */
							for(String whereE:whereExp)
								for(String whereI:whereImp)
								{
									if(!whereE.equals("d_datekey = lo_commitdate"))
									{
										if(whereE.equals(whereI))
										{
											if(!whereFinal.contains(whereE))
											{
												if(!whereFinal.isEmpty())
													whereFinal += " AND ";
												whereFinal += whereE; 
											}
										}
									}
								} //fim - for(s)
						} //fim - if
					} //fim - while
				} //fim - while
			} //fim - for
			
			System.out.println(" - whereFinal: "+whereFinal);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return whereFinal;
	}
		
	public /*ArrayList<String>*/String primaryKeys(String atributo)
	{
		//System.out.println("primaryKeys");
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		String tabela, coluna, pk;
		/*ArrayList<String>*/ String pks = ""/*new ArrayList<String>()*/;
		
		ResultSet rsTables;
		ResultSet rsColumns;
		ResultSet rsExpConstraints;
		
		try 
		{
			DatabaseMetaData md = con.getMetaData();
			String[] types = {"TABLE"};
			rsTables = md.getTables(null, "public", "%", types);
			
				rsTables = md.getTables(null, "public", "%", types);
				
				while(rsTables.next())
				{
					tabela = rsTables.getString(3); //3 == tabela
					//System.out.println("tabela: "+tabela);
					rsColumns = md.getColumns(null, "public", tabela, "%");
					
					while(rsColumns.next())
					{
						coluna = rsColumns.getString(4); //4 == coluna
						//System.out.println("coluna: "+coluna);
						
						if(coluna.equals(atributo))
						{
							//System.out.println("tabela: "+tabela);
														
							rsExpConstraints = md.getExportedKeys(null, "public", tabela);
							rsExpConstraints.next();
							//while(rsExpConstraints.next())
							//{
								pk = rsExpConstraints.getString(4);
								pks = pk;
							//}
						}
					} //fim - while
				} //fim - while
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return pks;
	}
	
	public ArrayList<String> foreignKeys(String atributo)
	{
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		String tabela, coluna, fk;
		ArrayList<String> fks = new ArrayList<String>();
		
		ResultSet rsTables;
		ResultSet rsColumns;
		ResultSet rsExpConstraints;
		
		try 
		{
			DatabaseMetaData md = con.getMetaData();
			String[] types = {"TABLE"};
			rsTables = md.getTables(null, "public", "%", types);
			
				rsTables = md.getTables(null, "public", "%", types);
			
				while(rsTables.next())
				{
					tabela = rsTables.getString(3); //3 == tabela
					rsColumns = md.getColumns(null, "public", tabela, "%");
					
					while(rsColumns.next())
					{
						coluna = rsColumns.getString(4); //4 == coluna
						
						if(coluna.equals(atributo))
						{
							//System.out.println("tabela: "+tabela);
														
							rsExpConstraints = md.getExportedKeys(null, "public", tabela);
							
							while(rsExpConstraints.next())
							{
								//System.out.println("   exp");
								//pk = rsExpConstraints.getString(4);
								fk = rsExpConstraints.getString(8); 
								//System.out.println("    -> "+ pk + " = " + fk);
								fks.add(fk);
							}
						}
					} //fim - while
				} //fim - while
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		//System.out.println(fks);
		return fks;
	}
	
	/***
	 * Para cada chave estrangeira, pega as tabelas que são referenciadas. 
	 * Por exemplo: s_city_fk liga supplier e city, que são as tabelas retornadas.
	 * @param colunas
	 * @return arraylist<string>
	 */
	public ArrayList<String> columnForeignKeys(String colunas)
	{
		ArrayList<String> columns = new ArrayList<String>(Arrays.asList(colunas.split(", ")));
		ArrayList<String> tabelas = new ArrayList<String>();
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		String tabela, coluna, fk;
		String tableOne="", tableTwo="";
		
		ResultSet rsTables;
		ResultSet rsColumns;
		ResultSet rsImpConstraints;
		
		try 
		{
			for(String column:columns) {
				//System.out.println(" ---->"+column);
				DatabaseMetaData md = con.getMetaData();
				String[] types = {"TABLE"};
				rsTables = md.getTables(null, "public", "%", types);
			
				while(rsTables.next())
				{
					tabela = rsTables.getString(3); //3 == tabela
					rsColumns = md.getColumns(null, "public", tabela, "%");
					
					while(rsColumns.next())
					{
						coluna = rsColumns.getString(4); //4 == coluna
						
						if(coluna.equals(column))
						{
							//System.out.println(" ------>"+coluna);
							rsImpConstraints = md.getImportedKeys(null, "public", tabela);
							
							while(rsImpConstraints.next())
							{
								fk = rsImpConstraints.getString(8); 
								if(fk.equals(column))
								{
									tableOne = rsImpConstraints.getString(3);
									tableTwo = rsImpConstraints.getString(7);
									System.out.println(" -----------"+tableOne+"_"+tableTwo);
									tabelas.add(tableOne+"_"+tableTwo);
								}
							}
						}
					} //fim - while
				} //fim - while
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return tabelas;
	}
	
	/**
	 * Pega as chaves primárias das chaves estrangeiras passadas por parâmetro.
	 * 'Retrieves a description of the primary key columns that are referenced by a table's foreign key columns.'
	 * @param colunas
	 * @return
	 */
	public ArrayList<String> takeForeignKeys(String colunas)
	{
		ArrayList<String> columns = new ArrayList<String>(Arrays.asList(colunas.split(", ")));
		ArrayList<String> fks = new ArrayList<String>();
		InterfacePool pool = new Pool();
		Connection con = pool.getConnection();
		String tabela, coluna, fk="";
		
		ResultSet rsTables;
		ResultSet rsColumns;
		ResultSet rsImpConstraints;
		
		try 
		{
			for(String column:columns) {
				//System.out.println(" ---->"+column);
				DatabaseMetaData md = con.getMetaData();
				String[] types = {"TABLE"};
				rsTables = md.getTables(null, "public", "%", types);
			
				while(rsTables.next())
				{
					tabela = rsTables.getString(3); //3 == tabela
					rsColumns = md.getColumns(null, "public", tabela, "%");
					
					while(rsColumns.next())
					{
						coluna = rsColumns.getString(4); //4 == coluna
						
						if(coluna.equals(column))
						{
							//System.out.println(" ------>"+coluna);
							rsImpConstraints = md.getImportedKeys(null, "public", tabela);
							
							while(rsImpConstraints.next())
							{
								fk = rsImpConstraints.getString(8); 
								if(fk.equals(column))
								{
									fks.add(fk);
								}
							}
						}
					} //fim - while
				} //fim - while
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return fks;
	}
	
}
