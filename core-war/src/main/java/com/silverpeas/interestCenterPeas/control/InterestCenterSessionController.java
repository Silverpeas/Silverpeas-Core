/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.interestCenterPeas.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.interestCenter.InterestCenterRuntimeException;
import com.silverpeas.interestCenter.control.InterestCenterService;
import com.silverpeas.interestCenter.model.InterestCenter;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import org.silverpeas.util.ServiceProvider;

public class InterestCenterSessionController extends AbstractComponentSessionController {

  private InterestCenterService interestCS =
      ServiceProvider.getService(InterestCenterService.class);

  /**
   * Constructor Creates new InterestCenter Session Controller
   */
  public InterestCenterSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.interestCenterPeas.multilang.interestCenterBundle",
        "org.silverpeas.interestCenterPeas.settings.interestCenterPeasIcons");
  }

  /**
   * checkServiceInjection checks if interestCenterService has been injected by CDI service
   * provider
   */
  private void checkServiceInjection() {
    if (interestCS == null) {
      throw new InterestCenterRuntimeException(
          "InterestCenterSessionController.checkServiceInjection()", "",
          "root.EX_CANT_GET_REMOTE_OBJECT");
    }
  }

  /**
   * Method getICByUserId returns ArrayList of all InterestCenter objects for user given by userId
   */
  public List<InterestCenter> getICByUserId() throws RemoteException {
    checkServiceInjection();
    return interestCS.getICByUserID(Integer.parseInt(getUserId()));
  }

  /**
   * Method getICByPK returns InterestCenter object by pk
   */
  public InterestCenter getICByPK(int pk) throws RemoteException {
    checkServiceInjection();
    return interestCS.getICByID(pk);
  }

  /**
   * Method createIC creates new InterestCenter
   */
  public void createIC(InterestCenter icToCreate) throws RemoteException {
    checkServiceInjection();
    interestCS.createIC(icToCreate);
  }

  /**
   * Method updateIC updates existing InterestCenter
   */
  public void updateIC(InterestCenter icToUpdate) throws RemoteException {
    checkServiceInjection();
    interestCS.updateIC(icToUpdate);
  }

  /**
   * Method removeICByPKs removes InterestCenter objects corresponding to PKs from given ArrayList
   */
  public void removeICByPKs(String[] iDs) throws RemoteException {
    checkServiceInjection();
    List<Integer> pkToRemove = new ArrayList<>();
    for (String id : iDs) {
      pkToRemove.add(Integer.valueOf(id));
    }
    interestCS.removeICByPK(pkToRemove, getUserId());
  }

  /**
   * Method removeICByPK removes InterestCenter object corresponding to given PK
   */
  public void removeICByPK(int pk) throws RemoteException {
    checkServiceInjection();
    interestCS.removeICByPK(pk);
  }

  public boolean isICExists(String nameIC) throws RemoteException {
    List<InterestCenter> icList = getICByUserId();
    for (InterestCenter ic : icList) {
      if (nameIC.equals(ic.getName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void close() {
    if (interestCS != null) {
      interestCS = null;
    }
  }
}
