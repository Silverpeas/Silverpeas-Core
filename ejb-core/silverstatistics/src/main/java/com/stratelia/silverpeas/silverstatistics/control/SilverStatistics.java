/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silverstatistics.control;

/*
 * CVS Informations
 *
 * $Id: SilverStatistics.java,v 1.1.1.1 2002/08/06 14:47:53 nchaix Exp $
 *
 * $Log: SilverStatistics.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.4  2002/04/05 08:42:20  mguillem
 * SilverStatistics
 *
 * Revision 1.3  2002/03/25 08:05:26  mguillem
 * SilverStatistics
 *
 * Revision 1.2  2002/03/14 08:49:03  mguillem
 * SilverStatistics
 *
 * Revision 1.1  2002/03/12 17:11:13  sleroux
 * SilverStatistics
 *
 * Revision 1.17  2002/01/11 12:40:30  neysseri
 * Stabilisation Lot 2 : Exceptions et Silvertrace
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface SilverStatistics extends javax.ejb.EJBObject {
  public void putStats(String typeOfStats, String data)
      throws java.rmi.RemoteException;

  public void makeStatAllCumul() throws java.rmi.RemoteException;

  public void makeVolumeAlimentationForAllComponents()
      throws java.rmi.RemoteException;
}
