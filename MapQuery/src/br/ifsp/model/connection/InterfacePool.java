package br.ifsp.model.connection;

import java.sql.Connection;

public interface InterfacePool {
	public abstract Connection getConnection();
	public void liberarConnection(Connection con);
}
