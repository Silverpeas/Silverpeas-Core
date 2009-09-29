package com.stratelia.webactiv.persistence;

import com.stratelia.webactiv.util.WAPrimaryKey;

public interface SilverpeasBeanIntf {

  public WAPrimaryKey getPK();

  public void setPK(WAPrimaryKey value);

  public int _getConnectionType();

  public String _getDatasourceName();

  public JdbcData _getJdbcData();

  public String _getTableName();

  public String getSureString(String theString);
}
