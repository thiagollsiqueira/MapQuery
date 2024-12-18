package br.ifsp.model.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.bean.Tabela;
import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.connection.Pool;
import br.ifsp.model.dao.IndicesDAO;

public class QueryIndexAttr implements InterfaceCommand {

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter pw;
		InterfacePool pool = new Pool();
		IndicesDAO indDAO = new IndicesDAO(pool);
		Tabela tabela = new Tabela();
				
		try 
		{
			pw = response.getWriter();

			String ibj = request.getParameter("ibj");
			String colunas = indDAO.lerColunas(ibj);
			ArrayList<String> attr = tabela.columnForeignKeys(colunas);
			ArrayList<String> attribute = tabela.takeForeignKeys(colunas);
			
			this.resposta(pw, attr, attribute);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void resposta(PrintWriter pw, ArrayList<String> attributes, ArrayList<String> attribute) {
		String strs = "{\"attributes\": \"";
			for(int i=0; i<attributes.size(); i++)
			{
				if(i>0)
					strs += ", ";
				strs += attributes.get(i);
			}
			strs += "\", \"attribute\": \"";
			for(int i=0; i<attribute.size(); i++)
			{
				if(i>0)
					strs += ", ";
				strs += attribute.get(i);
			}
			strs += "\"}";
		
		//System.out.println(strs);
		pw.print(strs);
		
	}	

}
