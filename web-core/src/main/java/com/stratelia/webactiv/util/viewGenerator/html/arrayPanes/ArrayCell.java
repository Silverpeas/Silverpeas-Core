/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

/*
 * CVS Informations
 * 
 * $Id: ArrayCell.java,v 1.1.1.1 2002/08/06 14:48:19 nchaix Exp $
 * 
 * $Log: ArrayCell.java,v $
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
public class ArrayCell {
  final static public int CELLSORT_CASE_INSENSITIVE = 1;
  final static public int CELLSORT_CASE_SENSITIVE = 2;

  /**
   * the default sort mode, that may or may not be interpreted by the
   * descendants of this class, depending on their contents. They could define
   * other modes, but the most common sould reasonnably be put in here
   */
  protected int m_SortMode = CELLSORT_CASE_SENSITIVE;
  private ArrayLine line;
  private String css = null;

  /**
   * Constructor declaration
   * 
   * 
   * @param line
   * 
   * @see
   */
  public ArrayCell(ArrayLine line) {
    this.line = line;
  }

  /**
   * Method declaration
   * 
   * 
   * @param css
   * 
   * @see
   */
  public void setStyleSheet(String css) {
    this.css = css;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getStyleSheet() {
    if (css != null) {
      return css;
    }
    if (line.getStyleSheet() != null) {
      return line.getStyleSheet();
    }
    return "ArrayCell";
  }

  /**
   * Method declaration
   * 
   * 
   * @param mode
   * 
   * @see
   */
  public void setSortMode(int mode) {
    m_SortMode = mode;
  }

}
