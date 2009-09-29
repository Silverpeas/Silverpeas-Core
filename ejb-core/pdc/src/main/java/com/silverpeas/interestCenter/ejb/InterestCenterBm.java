/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package com.silverpeas.interestCenter.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;

import com.silverpeas.interestCenter.model.InterestCenter;

/**
 * InterestCenterBm remote inteface
 */
public interface InterestCenterBm extends javax.ejb.EJBObject {

  /**
   * @return a list of <code>InterestCenter</code>s by user id provided
   */
  public ArrayList getICByUserID(int userID) throws RemoteException;

  /**
   * @param icPK
   *          <code>InterestCenter</code> id
   * @return InterestCenter by its id
   */
  public InterestCenter getICByID(int icPK) throws RemoteException;

  /**
   * @return id of <code>InterestCenter</code> created
   */
  public int createIC(InterestCenter ic) throws RemoteException;

  /**
   * perform updates of provided InterestCenter
   */
  public void updateIC(InterestCenter ic) throws RemoteException;

  /**
   * @param pks
   *          ArrayList of <code>java.lang.Integer</code> - id's of
   *          <code>InterestCenter</code>s to be deleted
   */
  public void removeICByPK(ArrayList pks) throws RemoteException;

  /**
   * @param pk
   *          an id of <code>InterestCenter</code> to be deleted
   */
  public void removeICByPK(int pk) throws RemoteException;

}
