package br.ifsp.model.command;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.bean.SBindex;
import br.ifsp.model.bean.Tabela;
import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.connection.Pool;
import br.ifsp.model.dao.IndicesDAO;

public class Build implements InterfaceCommand {

	private IndicesDAO indiceDAO;
	InterfacePool pool = new Pool();
	
	public Build() {
		super();
		this.indiceDAO = new IndicesDAO(pool);
	}

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter pw;
		Tabela tabela = new Tabela();
		
		try 
		{
			pw = response.getWriter();
			
			ArrayList<String> espaciais = new ArrayList<String>();
			espaciais = tabela.colunasEspaciais();
			
			String indexName = request.getParameter("name");
			System.out.println("\nnome do índice: "+ indexName);
			
			String atributos = request.getParameter("atributos");
			System.out.println(" - atributos: "+atributos);
			
			//tira os colchetes enviados do javascript (apenas indicavam que entre eles estariam os atributos do índice a ser criado)
			atributos = atributos.replace("[", "");
			atributos = atributos.replace("]", "");
			ArrayList<String> geoms = new ArrayList<String>();
			
			if(atributos.isEmpty())
			{
				//mensagem de erro
				System.out.println(" --------Erro na construção do IBJ.");
				this.resposta(pw, "Erro na construção do IBJ: selecione os atributos necessários.");
			}
			else
			{				
				ArrayList<String> attr = new ArrayList<String>(Arrays.asList(atributos.split("\\,")));
				for(int a=0; a<attr.size(); a++)
					for(int e=0; e<espaciais.size(); e++)
				{
					if(attr.get(a).equals(espaciais.get(e)))
					{
						String table = attr.get(a).replace("_geo", "");
						System.out.println(" - table (build): "+table);
						
						//verifica a existência do SB-index: se não existir, ele será construído
						File path = new File("C:\\Users\\RenataNote\\workspace\\MapQuery\\"+table+".ser");
						if(!path.getAbsoluteFile().exists())
						{
							System.out.println(" - sb-index: será construído");
							SBindex sb = new SBindex(path, 4096, 1, table+"_geo", table+"_pk", table);
							try 
							{
								sb.build();
							} 
							catch (Exception ex) 
							{
								System.out.println(" -------Erro na construção do SB-index");
								this.resposta(pw, "Erro na construção do índice SB-index.");
								ex.printStackTrace();
							}
						}
						else
						{
							System.out.println(" - sb-index: já existe");
						}
						//não precisa da chave primária//attr.add(tabela.primaryKeys(attr.get(a))); //pega a chave primária (city_pk, nation_pk, region_pk)
						geoms.add(attr.get(a));
						attr.remove(a); //o atributo espacial será usado apenas para o SB-index
						System.out.println(attr);
					}
				}
				
				//COLOCAR RETURN BOOLEAN?--------------------------------------------------------
				//construção do ibj
				indiceDAO.initializeIbj(attr, indexName, geoms);
				this.resposta(pw, "Construção realizada com sucesso.");				
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void resposta(PrintWriter pw, String result) {
		String str = "{\"result\": \""+result+"\"}";	
		pw.print(str);
	}	
}
