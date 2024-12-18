package br.ifsp.model.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.connection.Pool;
import br.ifsp.model.dao.IndicesDAO;

public class QueryLayers implements InterfaceCommand {

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		PrintWriter pw;
		InterfacePool pool = new Pool();
		IndicesDAO indDAO = new IndicesDAO(pool);
		try 
		{
			pw = response.getWriter();

			String ibj = request.getParameter("ibj");
			ArrayList<String> geoms = (ArrayList<String>) indDAO.getGeoms(ibj);
			
			this.resposta(pw, geoms);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void resposta(PrintWriter pw, ArrayList<String> geoms) {
		String strs = "{\"geoms\": \"";
			for(int i=0; i<geoms.size(); i++)
			{
				if(i>0)
					strs += ", ";
				strs += geoms.get(i);
			}
			strs += "\"}";
		
		//System.out.println(strs);
		pw.print(strs);
		
	}	


}
