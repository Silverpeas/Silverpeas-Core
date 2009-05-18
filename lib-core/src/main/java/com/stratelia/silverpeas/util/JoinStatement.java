package com.stratelia.silverpeas.util;

import java.util.List;

/**
 * This is the data structure that represents one part of a Join Statement
 * 
 */
public class JoinStatement {
  private String sTable = "";
  private String sWhere = "";
  private List<String> alTables = null;
  private List<String> alKeys = null;

  public JoinStatement() {
  }

  public void setTable(String sGivenTable) {
    sTable = sGivenTable;
  }

  public void setTables(List<String> alGivenTables) {
    alTables = alGivenTables;
  }

  public List<String> getTables() {
    return alTables;
  }

  public String getTable(int position) {
    if (alTables != null) {
      return (String) alTables.get(position);
    } else {
      return sTable;
    }
  }

  public void setJoinKeys(List<String> alGivenJoinKey) {
    this.alKeys = alGivenJoinKey;
  }

  public List<String> getJoinKeys() {
    return this.alKeys;
  }

  public String getJoinKey(int position) {
    return (String) alKeys.get(position);
  }

  public void setWhere(String sGivenWhere) {
    sWhere = sGivenWhere;
  }

  public String getWhere() {
    return sWhere;
  }
}
