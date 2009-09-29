/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

/*
 * CVS Informations
 * 
 * $Id: ArrayPaneStatusBean.java,v 1.2 2005/12/30 18:47:39 neysseri Exp $
 * 
 * $Log: ArrayPaneStatusBean.java,v $
 * Revision 1.2  2005/12/30 18:47:39  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.3  2002/01/04 14:04:23  mmarengo
 * Stabilisation Lot 2
 * SilverTrace
 * Exception
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class ArrayPaneStatusBean {
  private int firstVisibleLine = 0;
  private int maximumVisibleLine = 10;
  private int sortColumn = 0; // no column is sorted by default

  /**
   * Method declaration
   * 
   * 
   * @param firstVisibleLine
   * 
   * @see
   */
  public void setFirstVisibleLine(int firstVisibleLine) {
    this.firstVisibleLine = firstVisibleLine;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getFirstVisibleLine() {
    return firstVisibleLine;
  }

  /**
   * Method declaration
   * 
   * 
   * @param maximumVisibleLine
   * 
   * @see
   */
  public void setMaximumVisibleLine(int maximumVisibleLine) {
    this.maximumVisibleLine = maximumVisibleLine;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getMaximumVisibleLine() {
    return maximumVisibleLine;
  }

  /**
   * Method declaration
   * 
   * 
   * @param sortColumn
   * 
   * @see
   */
  public void setSortColumn(int sortColumn) {
    this.sortColumn = sortColumn;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getSortColumn() {
    return sortColumn;
  }
}