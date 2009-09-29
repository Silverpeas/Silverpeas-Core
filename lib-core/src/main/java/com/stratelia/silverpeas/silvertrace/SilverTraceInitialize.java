/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silvertrace;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;

/*
 * CVS Informations
 * 
 * $Id: SilverTraceInitialize.java,v 1.3 2004/11/05 14:50:05 neysseri Exp $
 * 
 * $Log: SilverTraceInitialize.java,v $
 * Revision 1.3  2004/11/05 14:50:05  neysseri
 * Nettoyage sources
 *
 * Revision 1.2  2002/10/09 07:41:03  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.3  2002/01/03 14:10:22  tleroi
 * Stabilisation Lot2 - Traces et Exceptions
 *
 * Revision 1.2  2001/12/19 14:29:18  tleroi
 * no message
 * 
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class SilverTraceInitialize implements IInitialize {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public SilverTraceInitialize() {
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean Initialize() {
    // Initialize SilverTrace
    SilverTrace sti = new SilverTrace();

    return true;
  }

}
