package com.stratelia.webactiv.persistence;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class SilverpeasBean implements SilverpeasBeanIntf, Serializable {

  private WAPrimaryKey pk;
  public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

  public SilverpeasBean() {
    setPK(new IdPK());
  }

  public WAPrimaryKey getPK() {
    return pk;
  }

  public void setPK(WAPrimaryKey value) {
    pk = value;
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_EJBDATASOURCE_SILVERPEAS;
  }

  public String _getDatasourceName() {
    return null;
  }

  public JdbcData _getJdbcData() {
    return null;
  }

  public String _getTableName() {
    return null;
  }

  public String getSureString(String theString) {
    return (theString == null) ? "" : theString;
  }
}
