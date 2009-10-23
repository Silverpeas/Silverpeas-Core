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

package com.stratelia.webactiv.util.publication.ejb;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBObject;

import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/*
 * CVS Informations
 * 
 * $Id: Publication.java,v 1.6 2008/03/26 13:15:50 neysseri Exp $
 * 
 * $Log: Publication.java,v $
 * Revision 1.6  2008/03/26 13:15:50  neysseri
 * no message
 *
 * Revision 1.5  2007/12/03 14:53:38  neysseri
 * no message
 *
 * Revision 1.4  2005/12/02 13:11:51  neysseri
 * Ajout d'un methode pour supprimer l'image
 *
 * Revision 1.3  2005/05/19 14:54:15  neysseri
 * Possibilite de supprimer les Voir Aussi
 *
 * Revision 1.2  2004/02/06 18:48:38  neysseri
 * Some useless methods removed !
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.5  2002/07/30 07:26:00  nchaix
 * Merge branche B200006
 *
 * Revision 1.4.30.1  2002/07/22 10:04:17  mnikolaenko
 * no message
 *
 * Revision 1.4  2002/01/11 12:40:46  neysseri
 * Stabilisation Lot 2 : Exceptions et Silvertrace
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface Publication extends EJBObject {

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public PublicationDetail getDetail() throws RemoteException, SQLException;

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public CompletePublication getCompletePublication() throws RemoteException,
      SQLException;

  /**
   * Method declaration
   * 
   * 
   * @param pubDetail
   * 
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public void setDetail(PublicationDetail pubDetail) throws RemoteException,
      SQLException;

  public void removeImage() throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param fatherPK
   * 
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public void addFather(NodePK fatherPK) throws RemoteException, SQLException;

  public void move(NodePK fatherPK) throws RemoteException, SQLException;

  /**
   * Method declaration
   * 
   * 
   * @param fatherPK
   * 
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public void removeFather(NodePK fatherPK) throws RemoteException,
      SQLException;

  /**
   * Method declaration
   * 
   * 
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public void removeAllFather() throws RemoteException, SQLException;

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public Collection getAllFatherPK() throws RemoteException, SQLException;

  public Collection getAllFatherPK(String sorting) throws RemoteException,
      SQLException;

  /**
   * Method declaration
   * 
   * 
   * @param modelPK
   * @param infos
   * 
   * @throws RemoteException
   * @throws SQLException
   * @throws UtilException
   * 
   * @see
   */
  public void createInfoDetail(ModelPK modelPK, InfoDetail infos)
      throws RemoteException, UtilException, SQLException;

  /**
   * Method declaration
   * 
   * 
   * @param modelPK
   * @param infos
   * 
   * @throws RemoteException
   * @throws SQLException
   * @throws UtilException
   * 
   * @see
   */
  public void createInfoModelDetail(ModelPK modelPK, InfoDetail infos)
      throws RemoteException, UtilException, SQLException;

  /**
   * Method declaration
   * 
   * 
   * @param infos
   * 
   * @throws RemoteException
   * @throws SQLException
   * @throws UtilException
   * 
   * @see
   */
  public void updateInfoDetail(InfoDetail infos) throws RemoteException,
      UtilException, SQLException;

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public InfoDetail getInfoDetail() throws RemoteException, SQLException;

  public void deleteInfoLinks(List pubIds) throws RemoteException, SQLException;
}