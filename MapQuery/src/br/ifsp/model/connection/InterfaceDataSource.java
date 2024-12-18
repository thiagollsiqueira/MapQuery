//define como obter as conexoes
package br.ifsp.model.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface InterfaceDataSource {
	public abstract Connection getconnection() throws SQLException;

}
