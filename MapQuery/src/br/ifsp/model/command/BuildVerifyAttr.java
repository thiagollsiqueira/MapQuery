package br.ifsp.model.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.bean.Tabela;

public class BuildVerifyAttr implements InterfaceCommand {

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter pw;
		
		String atributo = request.getParameter("espacial");
		
		try 
		{
			pw = response.getWriter();

			Tabela table = new Tabela();
			ArrayList<String> fks = table.foreignKeys(atributo);
						
			this.resposta(pw, fks);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void resposta(PrintWriter pw, ArrayList<String> foreignKeys) {
		String strs = "";

		strs += "{\"fks\":\"";
		for(int f=0; f<foreignKeys.size(); f++)
		{
			strs += foreignKeys.get(f);
			if(f < foreignKeys.size()-1)
				strs += ", ";
		}
		strs += "\"}";
		
		//System.out.println(strs);
		pw.print(strs);
		
	}

}
