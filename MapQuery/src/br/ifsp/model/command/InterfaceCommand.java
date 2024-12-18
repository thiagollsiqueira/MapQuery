package br.ifsp.model.command;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface InterfaceCommand {
	public String execute(HttpServletRequest request, HttpServletResponse response) throws SQLException;
}
