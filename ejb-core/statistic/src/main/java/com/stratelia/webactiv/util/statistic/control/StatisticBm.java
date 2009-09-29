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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package com.stratelia.webactiv.util.statistic.control;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBObject;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/*
 * CVS Informations
 * 
 * $Id: StatisticBm.java,v 1.5 2007/06/27 15:02:56 sfariello Exp $
 * 
 * $Log: StatisticBm.java,v $
 * Revision 1.5  2007/06/27 15:02:56  sfariello
 * Ajout détail des lectures par utilisateur
 *
 * Revision 1.4  2007/06/22 16:29:48  sfariello
 * no message
 *
 * Revision 1.3  2007/06/14 08:37:55  neysseri
 * no message
 *
 * Revision 1.2.2.1  2007/05/23 15:54:41  sfariello
 * no message
 *
 * Revision 1.2  2007/01/11 13:40:05  sfariello
 * Généralisation des statistiques aux foreignPK
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.5  2001/12/26 12:01:47  nchaix
 * no message
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface StatisticBm extends EJBObject {

  /**
   * Method declaration
   * 
   * @param userId
   * @param node
   * @param pub
   * @deprecated : utiliser la fonction addStat(String userId, ForeignPK
   *             foreignPK, int action, String objectType)
   * @throws RemoteException
   * 
   * @see
   */
  public void addReading(String userId, NodePK node, PublicationPK pub)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * @param userId
   * @param foreignPK
   * @throws RemoteException
   * 
   * @see
   */
  public void addStat(String userId, ForeignPK foreignPK, int action,
      String objectType) throws RemoteException;

  /**
   * Method declaration
   * 
   * @param pub
   * @deprecated : utiliser la fonction getHistoryByAction(ForeignPK foreignPK)
   * @return
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getReadingHistoryByPublication(PublicationPK pub)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * @param foreignPK
   * @return
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getHistoryByAction(ForeignPK foreignPK, int action,
      String objectType) throws RemoteException;

  public Collection getHistoryByObjectAndUser(ForeignPK foreignPK, int action,
      String objectType, String userId) throws RemoteException;

  public Collection getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType) throws RemoteException;

  /**
   * Method declaration
   * 
   * @param foreignPK
   * @return
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteHistoryByAction(ForeignPK foreignPK, int action,
      String objectType) throws RemoteException;

  /**
   * Method declaration
   * 
   * @param fatherPK
   * @deprecated : A SUPPRIMER APRES TESTS
   * @return
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getNodesUsage(NodePK fatherPK) throws RemoteException;

  /**
   * Method declaration
   * 
   * @param foreignPKs
   * @param action
   * @return
   * @throws RemoteException
   * 
   * @see
   */
  public int getCount(List foreignPKs, int action, String objectType)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * @param foreignPK
   * @param action
   * @return
   * @throws RemoteException
   * 
   * @see
   */
  public int getCount(ForeignPK foreignPK, int action, String objectType)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * @param foreignPK
   * @return
   * @throws RemoteException
   * 
   * @see
   */
  public int getCount(ForeignPK foreignPK, String objectType)
      throws RemoteException;
}
