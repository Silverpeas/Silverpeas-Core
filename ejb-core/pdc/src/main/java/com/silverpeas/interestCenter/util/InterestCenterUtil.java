/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.interestCenter.util;

import com.silverpeas.interestCenter.InterestCenterRuntimeException;
import com.silverpeas.interestCenter.ejb.InterestCenterBm;
import com.silverpeas.interestCenter.ejb.InterestCenterBmHome;
import com.silverpeas.interestCenter.model.InterestCenter;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

import java.rmi.RemoteException;
import java.util.List;

public class InterestCenterUtil {

  private InterestCenterBm icEjb = null;

  /**
   * Constructor Creates new Interest Center Util Controller
   */

  public InterestCenterUtil() {
  }

  /**
   * Method initEJB initializes EJB
   */
  private void initEJB() {
    if (icEjb == null) {
      try {
        InterestCenterBmHome icEjbHome =
            EJBUtilitaire.getEJBObjectRef(JNDINames.INTEREST_CENTER_EJBHOME,
            InterestCenterBmHome.class);
        icEjb = icEjbHome.create();
      } catch (Exception e) {
        throw new InterestCenterRuntimeException("InterestCenterUtil.initEJB()",
            "root.EX_CANT_GET_REMOTE_OBJECT", "", e);
      }
    }
  }

  /**
   * Method getICByUserId returns ArrayList of all InterestCenter objects for user given by userId
   */
  public List<InterestCenter> getICByUserId(int userId) throws RemoteException {
    initEJB();
    return icEjb.getICByUserID(userId);
  }

  /**
   * Method getICByPK returns Interest Center given by id
   */
  public InterestCenter getICByID(int id) throws RemoteException {
    initEJB();
    return icEjb.getICByID(id);
  }

  /**
   * Method isICExists returns true if InterstCenter with given name is already exists, false in
   * other case
   */

  public int isICExists(String nameIC, int userId) throws RemoteException {
    List<InterestCenter> icList = getICByUserId(userId);
    for (InterestCenter ic : icList) {
      if (nameIC.equals(ic.getName())) {
        return ic.getId();
      }
    }
    return -1;
  }

  /**
   * Method createIC creates new InterestCenter
   */
  public int createIC(InterestCenter icToCreate) throws RemoteException {
    initEJB();
    int id = isICExists(icToCreate.getName(), icToCreate.getOwnerID());
    if (id != -1) {
      icEjb.removeICByPK(id);
    }
    return icEjb.createIC(icToCreate);
  }

}
