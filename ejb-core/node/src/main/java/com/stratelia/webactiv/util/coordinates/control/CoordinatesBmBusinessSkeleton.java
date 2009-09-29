/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.coordinates.control;

import java.util.Collection;
import java.util.ArrayList;
import java.rmi.RemoteException;
import com.stratelia.webactiv.util.coordinates.model.*;

/**
 * Interface declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public interface CoordinatesBmBusinessSkeleton {

  /**
   * Used only by the specific job'peas SmallAds This method must not be used by
   * an another Job'peas Instead, you must use getCoordinatesByFatherPaths()
   * 
   * @param fatherIds
   * @param pk
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getCoordinatesByFatherIds(ArrayList fatherIds,
      CoordinatePK pk) throws RemoteException;

  /**
   * 
   * 
   * @param fatherIds
   * @param pk
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getCoordinatesByFatherPaths(ArrayList fatherPaths,
      CoordinatePK pk) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * @param coordinatePoints
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public int addCoordinate(CoordinatePK pk, ArrayList coordinatePoints)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * @param coordinates
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteCoordinates(CoordinatePK pk, ArrayList coordinates)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * @param coordinatePoints
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteCoordinatesByPoints(CoordinatePK pk,
      ArrayList coordinatePoints) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param coordinateIds
   * @param pk
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public ArrayList getCoordinatesByCoordinateIds(ArrayList coordinateIds,
      CoordinatePK pk) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * @param point
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void addPointToAllCoordinates(CoordinatePK pk, CoordinatePoint point)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * @param nodeId
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getCoordinateIdsByNodeId(CoordinatePK pk, String nodeId)
      throws RemoteException;
}
