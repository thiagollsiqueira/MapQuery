//recebe a requisição e retorna um comando a ser executado

package br.ifsp.model.helper;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import br.ifsp.model.command.Build;
import br.ifsp.model.command.BuildOptions;
import br.ifsp.model.command.BuildVerifyAttr;
import br.ifsp.model.command.InterfaceCommand;
import br.ifsp.model.command.Map;
import br.ifsp.model.command.Query;
import br.ifsp.model.command.QueryIndexAttr;
import br.ifsp.model.command.QueryIndexes;
import br.ifsp.model.command.QueryLayers;
import br.ifsp.model.command.QueryText;
import br.ifsp.model.connection.InterfacePool;
import br.ifsp.model.connection.Pool;
import br.ifsp.model.dao.IndicesDAO;

public class Helper {
	private HashMap<String, InterfaceCommand> commandsMap;
	private HttpServletRequest request;
	
	private InterfacePool pool;
	
	public Helper()
	{
		this.pool = new Pool();
		
		commandsMap = new HashMap<String, InterfaceCommand>();

		commandsMap.put("map", new Map()); 
		commandsMap.put("query", new Query()); 
		commandsMap.put("buildOptions", new BuildOptions());
		commandsMap.put("build", new Build());
		commandsMap.put("ibjs", new QueryIndexes(new IndicesDAO(pool)));
		commandsMap.put("verifyAttr", new BuildVerifyAttr());
		commandsMap.put("queryText", new QueryText());
		commandsMap.put("queryIndexAttrt", new QueryIndexAttr());
		commandsMap.put("queryLayers", new QueryLayers());
	}
		
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	public InterfaceCommand getCommand()
	{
		return commandsMap.get(request.getParameter("cmd")); //cmd = command
	}
	
}
