//responsavel pelo gerenciamento de todas as requisicoes da aplicacao web --> delega para o helper para que ele decida o que fazer
//o helper transfere, entao, a execucao desse processamento para algum comando

package br.ifsp.model.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ifsp.model.command.InterfaceCommand;
import br.ifsp.model.helper.Helper;

/**
 * Servlet implementation class Controller
 */
public class Controller extends HttpServlet {
	
	   private static final long serialVersionUID = 1L;
       private Helper helper = new Helper();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Controller() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void init() throws ServletException {
    	// TODO Auto-generated method stub
    	super.init();
    	System.load("C:\\Windows\\system\\SBindex.dll");
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			processarRequisicao(request, response);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processarRequisicao(HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		helper.setRequest(request);
		InterfaceCommand comando = helper.getCommand();
		String pagina = comando.execute(request, response);
		if(pagina != null)
		{
		try {
			request.getRequestDispatcher(pagina).include(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			processarRequisicao(request, response);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
