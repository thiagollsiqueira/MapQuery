package br.ifsp.model.command;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.dao.MapaDAO;
import br.ifsp.model.connection.Pool;

//import com.google.gson.Gson;

public class Map implements InterfaceCommand {

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		
		InterfacePool pool = new Pool();
		MapaDAO mapa = new MapaDAO(pool);
		String table = request.getParameter("table");
		System.out.println("\ntable - mapear: "+table);
				
		try
		{
			ArrayList<String> rn = new ArrayList<String>();
			rn = mapa.getMap(table);
			
			//faz todas as modificações necessárias (coloca as coordenadas entre colchetes, com vírgulas etc) para facilitar a criação do objeto json
			//antes: "MULTIPOLYGON(((-89.733095 36.000608,-89.732774 36.000073,-89.720815 36.000793,-89.732569 36.000635)))"
			//depois: [-89.733095,36.000608],[-89.732774,36.000073],[-89.720815,36.000793],[-89.732569,36.000635]
			for(int i=0; i<rn.size(); i++)
			{
				rn.set(i, rn.get(i).substring(15));
				rn.set(i, rn.get(i).replace(",", "],["));
				rn.set(i, rn.get(i).replace(" ", ","));
				rn.set(i, rn.get(i).replace(")))", ""));
			}
			
			response.setContentType("text/json;charset=UTF-8");
			PrintWriter pw = response.getWriter();
			
			this.resposta(pw, rn);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return null;
	}
	
	private void resposta(PrintWriter pw, ArrayList<String> geoms) {
		ArrayList<String> strs = new ArrayList<String>();
		
		for(int i=0; i<geoms.size(); i++)
		{
			strs.add("{\"type\": \"Polygon\", \"coordinates\": [[["+geoms.get(i)+"]]]}");
			//System.out.println("str("+i+"): "+strs.get(i));
		}
		
		pw.print(strs);
	}
}
