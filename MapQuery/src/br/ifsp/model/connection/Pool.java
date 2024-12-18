package br.ifsp.model.connection;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;

public class Pool implements InterfacePool {

	private InterfaceDataSource ds;
	private ArrayBlockingQueue<Connection> conexoesLivres;
	private HashMap<String, Connection> conexoesUtilizadas;
	private int numeroMaximoConexoes;
	private ResourceBundle config;
	
	static Connection connection = null; 
	static Statement statement = null; 
	
	public Pool()
	{
		config = ResourceBundle.getBundle("br.ifsp.model.connection.bd");
		ds = new DataSource(config.getString("url"), config.getString("driver"), config.getString("usuario"), config.getString("senha"));
		numeroMaximoConexoes = Integer.parseInt(config.getString("numeroMaximoConexoes"));
		conexoesLivres = new ArrayBlockingQueue<Connection>(numeroMaximoConexoes, true);
		conexoesUtilizadas = new HashMap<String, Connection>();
	}
	

	@Override
	public Connection getConnection() {
		Connection con = null;
		
		try {
			if (conexoesUtilizadas.size() < numeroMaximoConexoes) {
				con = conexoesLivres.poll();
				if (con == null)
				{
					con = ds.getconnection();
				}
				else if(con.isClosed())
				{
					this.getConnection();
				}
				conexoesUtilizadas.put(con.toString(), con);
			}
		} catch (Exception e) {
			System.out.println("Problemas com o pool");
		}
		return con;
	}

	@Override
	public void liberarConnection(Connection con) {
		conexoesLivres.add(con);
		conexoesUtilizadas.remove(con.toString());
	}

}
