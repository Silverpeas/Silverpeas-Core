package com.stratelia.silverpeas.selectionPeas.jdbc;

/**
 * JDBC connection setting.
 * 
 * @author Antoine HEDIN
 */
public class JdbcConnectorSetting
{
	
	/**
	 * Connection setting.
	 */
	private String driverClassName = "";
	private String url = "";
	private String login = "";
	private String password = "";
	
	public JdbcConnectorSetting(String driverClassName, String url, String login, String password)
	{
		this.driverClassName = driverClassName;
		this.url = url;
		this.login = login;
		this.password = password;
	}
	
	public String getDriverClassName()
	{
		return driverClassName;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public String getLogin()
	{
		return login;
	}
	
	public String getPassword()
	{
		return password;
	}

}