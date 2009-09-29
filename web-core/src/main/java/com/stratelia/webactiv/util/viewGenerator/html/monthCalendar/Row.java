/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * Row.java
 * this object represent the row in week.
 * the event of week is represented by different row if the event have the same date.
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar
 * Created on 18 juin 2001, 10:26
 * @author Jean-Claude GROCCIA
 * jgroccia@silverpeas.com
 */

package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import java.util.Vector;

/*
 * CVS Informations
 * 
 * $Id: Row.java,v 1.1.1.1 2002/08/06 14:48:19 nchaix Exp $
 * 
 * $Log: Row.java,v $
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.3  2002/01/04 14:04:24  mmarengo
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
class Row extends Object {

  private Vector listEventRow = null;

  /**
   * Creates the new row
   */
  public Row(Event evt) {
    listEventRow = new Vector();
    listEventRow.addElement(evt);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public Row() {
    listEventRow = new Vector();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public Vector getListEvent() {
    return listEventRow;
  }

  /**
   * Method declaration
   * 
   * 
   * @param evt
   * 
   * @see
   */
  public void addEventIntRow(Event evt) {
    listEventRow.addElement(evt);
  }

}
