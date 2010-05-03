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

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * Interface declaration
 * @author
 */
public interface Publication extends EJBObject {

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public PublicationDetail getDetail() throws RemoteException, SQLException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public CompletePublication getCompletePublication() throws RemoteException,
      SQLException;

  /**
   * Method declaration
   * @param pubDetail
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void setDetail(PublicationDetail pubDetail) throws RemoteException,
      SQLException;
  public void setDetail(PublicationDetail pubDetail, boolean forceUpdateDate)  throws RemoteException,
      SQLException;

  public void removeImage() throws RemoteException;

  /**
   * Method declaration
   * @param fatherPK
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void addFather(NodePK fatherPK) throws RemoteException, SQLException;

  public void move(NodePK fatherPK) throws RemoteException, SQLException;

  /**
   * Method declaration
   * @param fatherPK
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void removeFather(NodePK fatherPK) throws RemoteException,
      SQLException;

  /**
   * Method declaration
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void removeAllFather() throws RemoteException, SQLException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public Collection<NodePK> getAllFatherPK() throws RemoteException, SQLException;

  public Collection<NodePK> getAllFatherPK(String sorting) throws RemoteException,
      SQLException;

  /**
   * Method declaration
   * @param modelPK
   * @param infos
   * @throws RemoteException
   * @throws SQLException
   * @throws UtilException
   * @see
   */
  public void createInfoDetail(ModelPK modelPK, InfoDetail infos)
      throws RemoteException, UtilException, SQLException;

  /**
   * Method declaration
   * @param modelPK
   * @param infos
   * @throws RemoteException
   * @throws SQLException
   * @throws UtilException
   * @see
   */
  public void createInfoModelDetail(ModelPK modelPK, InfoDetail infos)
      throws RemoteException, UtilException, SQLException;

  /**
   * Method declaration
   * @param infos
   * @throws RemoteException
   * @throws SQLException
   * @throws UtilException
   * @see
   */
  public void updateInfoDetail(InfoDetail infos) throws RemoteException,
      UtilException, SQLException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public InfoDetail getInfoDetail() throws RemoteException, SQLException;

  /**
   * Removes links between publications and the current publication
   * @param links list of links to remove
   * @throws SQLException
   */
  public void deleteInfoLinks(List<ForeignPK> links) throws SQLException, RemoteException;
}