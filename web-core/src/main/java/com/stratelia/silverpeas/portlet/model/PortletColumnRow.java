package com.stratelia.silverpeas.portlet.model;

public class PortletColumnRow {
  private int id;
  private int spaceId;
  private String columnWidth;
  private int nbCol;

  public int getId() {
    return id;
  }

  public void setId(int aId) {
    id = aId;
  }

  public int getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(int aSpaceId) {
    spaceId = aSpaceId;
  }

  public String getColumnWidth() {
    return columnWidth;
  }

  public void setColumnWidth(String aColumnWidth) {
    columnWidth = aColumnWidth;
  }

  public int getNbCol() {
    return nbCol;
  }

  public void setNbCol(int aNbCol) {
    nbCol = aNbCol;
  }

  public PortletColumnRow(int aId, int aSpaceId, String aColumnWidth, int aNbCol) {
    id = aId;
    spaceId = aSpaceId;
    columnWidth = aColumnWidth;
    nbCol = aNbCol;
  }
}
