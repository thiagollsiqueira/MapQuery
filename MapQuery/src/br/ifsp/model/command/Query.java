package br.ifsp.model.command;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.bean.QUERY_TYPE;
import br.ifsp.model.bean.SBindex;
import br.ifsp.model.bean.SpatialQueryWindow;
import br.ifsp.model.command.InterfaceCommand;
import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.dao.IndicesDAO;
import br.ifsp.model.dao.SbitvectorDAO;
import br.ifsp.model.connection.Pool;


public class Query implements InterfaceCommand{
	
	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
				
		InterfacePool pool = new Pool();
		IndicesDAO indiceDAO = new IndicesDAO(pool);
		SbitvectorDAO sbitDAO = new SbitvectorDAO(pool);
				
		String queryType = request.getParameter("queryType");
		
		int windows = Integer.parseInt(request.getParameter("q"));
		System.out.println("windows: "+windows);
		
		int w;
		
		System.out.println("ibj: "+request.getParameter("ibj"));
		sbitDAO.setTable(request.getParameter("ibj"));
		
		String atrib = "";
		String select = request.getParameter("select");
		String where = request.getParameter("where");
		String verify = where.substring(6);
		if(verify.isEmpty())
			where = "WHERE ";
		else
			where += " AND (";
		
		
		ArrayList<String> sbitvectors = new ArrayList<String>();
		
		for(w=0; w<windows; w++)
		{
			System.out.println("w: "+ w);
			String tipo = request.getParameter("type"+w);
			
			if(tipo.equals("polygon"))
			{
				atrib = request.getParameter("atributo"+w);
				String table = request.getParameter("table"+w);
				try
				{
					//ArrayList<String> sbitvectors = new ArrayList<String>();
					
					double[] queryWindow = new double[4]; 
					queryWindow[0] = Double.parseDouble(request.getParameter("a0"+w)); //xmin
					queryWindow[1] = Double.parseDouble(request.getParameter("a1"+w)); //ymin
					queryWindow[2] = Double.parseDouble(request.getParameter("b0"+w)); //xmax
					queryWindow[3] = Double.parseDouble(request.getParameter("b1"+w)); //ymax
					
					System.out.println("\n#\ntable: "+table+"\tqueryType: "+queryType);
					System.out.print("tipo: "+tipo+"\tcoord: ["+queryWindow[0]+", "
							+queryWindow[1]+", "+queryWindow[2]+", "+queryWindow[3]+"]\n");
					System.out.println("atributo: "+atrib);
					
					SpatialQueryWindow sqw;
					
					if(queryType.equals("irq"))
					{
						sqw = new SpatialQueryWindow(queryWindow, table, table+"_geo", table+"_pk", QUERY_TYPE.IRQ);
					}
					else if(queryType.equals("crq"))
					{
						sqw = new SpatialQueryWindow(queryWindow, table, table+"_geo", table+"_pk", QUERY_TYPE.CRQ);
					}
					else
					{
						sqw = new SpatialQueryWindow(queryWindow, table, table+"_geo", table+"_pk", QUERY_TYPE.ERQ);
					}
					
					System.out.println();
					SBindex sb = new SBindex(new File("C:\\Users\\Usuario\\workspace\\MapQuery\\"+table+".ser"), 4096, 1,  table+"_geo", table+"_pk", table);
					sbitvectors.add(sbitDAO.getSbitvectors(sqw, sb, table));
					//sqlSelect = sbitDAO.getSbitvectors(sqw, sb, table);
					System.out.println("--->sbitvectors["+w+"]: "+sbitvectors.get(w)+"\n");
					
					if (sbitvectors.get(w).isEmpty())
					{
						where += "";
					}
					else
					{
						if(w>0) 
							where += " OR ";
						where += "(";
						//substitui as chaves primárias do banco por atributos do ibj criado
						if(sbitvectors.get(w).contains("city"))
						{
							where += sbitvectors.get(w).replace("city", atrib);
						}
						else if (sbitvectors.get(w).contains("nation"))
						{
							where += sbitvectors.get(w).replace("nation", atrib);
						}
						else
						{
							where += sbitvectors.get(w).replace("region", atrib);
						}
						where += ")"; 
					}
					/*if (sbitvectors.get(w).isEmpty())
					{
						where += "";
					}
					else
					{						
						if(sbitvectors.get(w).isEmpty())
						{
							where += "";
						}
						else
						{
							if(w>0)
								where += " OR ";
							//else
								//where += " AND ";
							where += "("+ sbitvectors.get(w) + ")";
						}
					}
					/*if (sbitvectors.get(w).isEmpty())
					{
						where += "";
					}
					else
					{
						if(w>0)
							where += " OR ";
						else
							where += " AND ";
						where += "("+ sbitvectors.get(w) + ")"; 
					}*/
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}//fim do for
		//VERIFICAR ELSE
		/*else
		{
			String table = request.getParameter("table");
			
			String x = request.getParameter("a0");
			String y = request.getParameter("a1");
			
			coord = "["+x+", "+y+"]";
			
			System.out.println("\ntable: "+table);
			System.out.println("tipo: "+tipo+"	coord: "+coord);
		}*/

		
		String whereFinal = "";
		int empty = 0;
		for(int z=0; z<sbitvectors.size(); z++)
		{
			if(sbitvectors.get(z).isEmpty())
				empty++;
		}
		
		System.out.println("sbit.size(): "+sbitvectors.size());
		System.out.println("empty: "+empty);
		
		//se a cláusula where não estiver vazia
		if(!where.isEmpty())
		{
				whereFinal += where;
			
			//fecha as condições
			//if(w>0 && empty<windows-1)
			//{
				if (where.contains("AND"))
				{
					whereFinal += ")";
				}
			//}
			System.out.println("\nWhereFinal: " + whereFinal);
			
			//System.out.println("---->>"+sbitDAO.getTable());
			ArrayList<String> result = new ArrayList<String>();
			if(empty != sbitvectors.size())
				result = (ArrayList<String>) indiceDAO.consultarFastBit(select, whereFinal, sbitDAO.getTable());
			System.out.println("result: "+result);
			
			
			response.setContentType("text/json;charset=UTF-8");
			PrintWriter pw;
			
			try 
			{
				pw = response.getWriter();
				//retornar o resultado da consulta
				this.resposta(pw, result);
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return null;
	}
	
	private void resposta(PrintWriter pw, ArrayList<String> result) {
		String strs = "{\"result\": \"";
		for(int i=0; i<result.size(); i++)
		{
			if(i>0)
				strs += "[ ";
			
			strs += result.get(i);
			if(i<result.size()-1)
				strs += "],";
		}
		strs += "\"}";
			
		pw.print(strs);
	}
}
