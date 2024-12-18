package br.ifsp.model.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.dao.IndicesDAO;

public class QueryIndexes implements InterfaceCommand {
	
	private IndicesDAO atributoDAO;
	
	public QueryIndexes(IndicesDAO atributoDAO) {
		super();
		this.atributoDAO = atributoDAO;
	}

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		
		//System.out.println("ConsultarIndices()");
		
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter pw;
		
		try 
		{
			pw = response.getWriter();
			this.resposta(pw, atributoDAO.getIndices());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; 
	}
	
	private void resposta(PrintWriter pw, List<String> indices) {
		String strs = "";
		
		strs = "{\"ibjs\": \"";
		for(int c=0; c<indices.size(); c++)
		{
			strs += indices.get(c);
			if(c < indices.size()-1)
				strs += ", ";
		}
		strs += "\"}";
		
		pw.print(strs);		
	}	
}
