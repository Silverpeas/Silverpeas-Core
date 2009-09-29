/*
 * WADataPage.java
 *
 * Created on 26 mars 2001, 09:58
 */

package com.stratelia.webactiv.util.datapaginator;

/**
 * 
 * @author jpouyadou
 * @version
 */
public interface WAItem {
  public int getFieldCount();

  public String getFirstField();

  public String getLastField();

  public String getNextField();

  public String getPreviousField();

  public void toggleFieldState();

  public String getFieldByName(String name);

  /**
   * If the data to be displayed is anchorable, this returns its anchor,
   * otherwise it returns null
   */
  public String getAnchorByName(String name);

  public void setDataPaginator(WADataPaginator parent);

  /**
   * This method returns the style (in the sense of element of a style sheet)
   * used to represent this item. It has to be held by the WAItem, because it is
   * the only one to know the meaning of the information it carries, and thus
   * the only one to know what, for example, needs to be emphasized via styles.
   * A null value means 'use default'
   */
  public String getStyle();
}
