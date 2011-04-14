/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
   * @param icPK <code>InterestCenter</code> id
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
   * @param pks ArrayList of <code>java.lang.Integer</code> - id's of <code>InterestCenter</code>s
   * to be deleted
   */
  public void removeICByPK(ArrayList pks) throws RemoteException;

  /**
   * @param pk an id of <code>InterestCenter</code> to be deleted
   */
  public void removeICByPK(int pk) throws RemoteException;

}
