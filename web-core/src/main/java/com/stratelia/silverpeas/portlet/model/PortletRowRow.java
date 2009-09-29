package com.stratelia.silverpeas.portlet.model;

public class PortletRowRow {
  private int id;
  private int InstanceId;
  private int portletColumnId;
  private int rowHeight;
  private int nbRow;

  public int getId() {
    return id;
  }

  public void setId(int aId) {
    id = aId;
  }

  public int getInstanceId() {
    return InstanceId;
  }

  public void setInstanceId(int aInstanceId) {
    InstanceId = aInstanceId;
  }

  public int getPortletColumnId() {
    return portletColumnId;
  }

  public void setPortletColumnId(int aPortletColumnId) {
    portletColumnId = aPortletColumnId;
  }

  public int getRowHeight() {
    return rowHeight;
  }

  public void setRowHeight(int aRowHeight) {
    rowHeight = aRowHeight;
  }

  public int getNbRow() {
    return nbRow;
  }

  public void setNbRow(int aNbRow) {
    nbRow = aNbRow;
  }

  public PortletRowRow(int aId, int aInstanceId, int aPortletColumnId,
      int aRowHeight, int aNbRow) {
    id = aId;
    InstanceId = aInstanceId;
    portletColumnId = aPortletColumnId;
    rowHeight = aRowHeight;
    nbRow = aNbRow;
  }
}
