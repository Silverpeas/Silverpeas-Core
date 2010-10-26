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

package com.stratelia.webactiv.util.coordinates.control;

import java.util.Collection;
import java.util.ArrayList;
import java.rmi.RemoteException;
import com.stratelia.webactiv.util.coordinates.model.*;
import java.util.List;

/**
 * Interface declaration
 * @author
 * @version %I%, %G%
 */
public interface CoordinatesBmBusinessSkeleton {

  /**
   * Used only by the specific job'peas SmallAds This method must not be used by an another Job'peas
   * Instead, you must use getCoordinatesByFatherPaths()
   * @param fatherIds
   * @param pk
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getCoordinatesByFatherIds(ArrayList fatherIds,
      CoordinatePK pk) throws RemoteException;

  /**
   * @param fatherIds
   * @param pk
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getCoordinatesByFatherPaths(ArrayList fatherPaths,
      CoordinatePK pk) throws RemoteException;

  /**
   * Method declaration
   * @param pk
   * @param coordinatePoints
   * @return
   * @throws RemoteException
   * @see
   */
  public int addCoordinate(CoordinatePK pk, List coordinatePoints)
      throws RemoteException;

  /**
   * Method declaration
   * @param pk
   * @param coordinates
   * @throws RemoteException
   * @see
   */
  public void deleteCoordinates(CoordinatePK pk, ArrayList coordinates)
      throws RemoteException;

  /**
   * Method declaration
   * @param pk
   * @param coordinatePoints
   * @throws RemoteException
   * @see
   */
  public void deleteCoordinatesByPoints(CoordinatePK pk,
      ArrayList coordinatePoints) throws RemoteException;

  /**
   * Method declaration
   * @param coordinateIds
   * @param pk
   * @return
   * @throws RemoteException
   * @see
   */
  public ArrayList getCoordinatesByCoordinateIds(ArrayList coordinateIds,
      CoordinatePK pk) throws RemoteException;

  /**
   * Method declaration
   * @param pk
   * @param point
   * @throws RemoteException
   * @see
   */
  public void addPointToAllCoordinates(CoordinatePK pk, CoordinatePoint point)
      throws RemoteException;

  /**
   * Method declaration
   * @param pk
   * @param nodeId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getCoordinateIdsByNodeId(CoordinatePK pk, String nodeId)
      throws RemoteException;
}
