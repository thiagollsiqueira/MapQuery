package br.ifsp.model.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.connection.Pool;
import br.ifsp.model.dao.IndicesDAO;

public class QueryText implements InterfaceCommand {

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter pw;
		InterfacePool pool = new Pool();
		IndicesDAO indDAO = new IndicesDAO(pool);
				
		try 
		{
			pw = response.getWriter();

			String ibj = request.getParameter("ibj");
			String colunas = indDAO.lerColunas(ibj);
			
			this.resposta(pw, colunas);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void resposta(PrintWriter pw, String attributes) {
		String strs = "{\"sql\":\"SELECT " + attributes + "\"}";
		
		//System.out.println(strs);
		pw.print(strs);
		
	}	
}
