package br.ifsp.model.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.bean.Tabela;

public class BuildOptions implements InterfaceCommand {

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter pw;
		
		try 
		{
			pw = response.getWriter();

			Tabela table = new Tabela();
			ArrayList<String> colunasEspaciais = table.colunasEspaciais();
			ArrayList<String> colunasConvencionais = table.colunasConvencionais();
						
			System.out.println("\ncolunas espaciais: "+colunasEspaciais);
			System.out.println("colunas convencionais: "+colunasConvencionais);
			this.resposta(pw, colunasEspaciais, colunasConvencionais);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void resposta(PrintWriter pw, ArrayList<String> spatialColumns, ArrayList<String> conventionalColumns) {
		String strs = "";
		
		//espaciais
		strs += "{\"espaciais\":\"";
		for(int s=0; s<spatialColumns.size(); s++)
		{
			strs += spatialColumns.get(s);
			if(s < spatialColumns.size()-1)
				strs += ", ";
		}
		strs += "\", ";
		
		//convencionais
		strs += "\"convencionais\":\"";
		for(int c=0; c<conventionalColumns.size(); c++)
		{
			strs += conventionalColumns.get(c);
			if(c < conventionalColumns.size()-1)
				strs += ", ";
		}
		strs += "\"}";
		
		//System.out.println(strs);
		pw.print(strs);
		
	}

}
