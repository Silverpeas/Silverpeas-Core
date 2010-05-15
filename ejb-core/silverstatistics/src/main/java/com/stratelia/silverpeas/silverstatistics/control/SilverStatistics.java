/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
 * @author
 */
public interface SilverStatistics extends javax.ejb.EJBObject {
  public void putStats(String typeOfStats, String data)
      throws java.rmi.RemoteException;

  public void makeStatAllCumul() throws java.rmi.RemoteException;

  public void makeVolumeAlimentationForAllComponents()
      throws java.rmi.RemoteException;
}
