package com.stratelia.silverpeas.selection;

public class SelectionJdbcParams implements SelectionExtraParams {
  private String driverClassName = "";
  private String url = "";
  private String login = "";
  private String password = "";
  private String tableName = "";
  private String columnsNames = "";
  private String formIndex = "";
  private String fieldsNames = "";

  public SelectionJdbcParams(String driverClassName, String url, String login,
      String password, String tableName, String columnsNames, String formIndex,
      String fieldsNames) {
    this.driverClassName = driverClassName;
    this.url = url;
    this.login = login;
    this.password = password;
    this.tableName = tableName;
    this.columnsNames = columnsNames;
    this.formIndex = formIndex;
    this.fieldsNames = fieldsNames;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public String getUrl() {
    return url;
  }

  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }

  public String getTableName() {
    return tableName;
  }

  public String getColumnsNames() {
    return columnsNames;
  }

  public String getParameter(String name) {
    if (name.equals("tableName")) {
      return tableName;
    } else if (name.equals("columnsNames")) {
      return columnsNames;
    } else if (name.equals("formIndex")) {
      return formIndex;
    } else if (name.equals("fieldsNames")) {
      return fieldsNames;
    }
    return null;
  }

}
