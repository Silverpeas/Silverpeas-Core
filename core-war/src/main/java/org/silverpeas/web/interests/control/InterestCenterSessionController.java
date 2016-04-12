/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.interests.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.pdc.interests.model.Interests;
import org.silverpeas.core.pdc.interests.service.InterestsRuntimeException;
import org.silverpeas.core.pdc.interests.service.InterestsService;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.ServiceProvider;

public class InterestCenterSessionController extends AbstractComponentSessionController {

  private InterestsService interestCS =
      ServiceProvider.getService(InterestsService.class);

  /**
   * Constructor Creates new Interests Session Controller
   */
  public InterestCenterSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.interests.multilang.interestsBundle",
        "org.silverpeas.interests.settings.interestsIcons");
  }

  /**
   * checkServiceInjection checks if interestCenterService has been injected by CDI service
   * provider
   */
  private void checkServiceInjection() {
    if (interestCS == null) {
      throw new InterestsRuntimeException(
          "InterestCenterSessionController.checkServiceInjection()", "",
          "root.EX_CANT_GET_REMOTE_OBJECT");
    }
  }

  /**
   * Method getInterestsByUserId returns ArrayList of all Interests objects for user given by userId
   */
  public List<Interests> getICByUserId() throws RemoteException {
    checkServiceInjection();
    return interestCS.getInterestsByUserId(Integer.parseInt(getUserId()));
  }

  /**
   * Method getInterestsByPK returns Interests object by pk
   */
  public Interests getICByPK(int pk) throws RemoteException {
    checkServiceInjection();
    return interestCS.getInterestsById(pk);
  }

  /**
   * Method createInterests creates new Interests
   */
  public void createIC(Interests icToCreate) throws RemoteException {
    checkServiceInjection();
    interestCS.createInterests(icToCreate);
  }

  /**
   * Method updateInterests updates existing Interests
   */
  public void updateIC(Interests icToUpdate) throws RemoteException {
    checkServiceInjection();
    interestCS.updateInterests(icToUpdate);
  }

  /**
   * Method removeICByPKs removes Interests objects corresponding to PKs from given ArrayList
   */
  public void removeICByPKs(String[] iDs) throws RemoteException {
    checkServiceInjection();
    List<Integer> pkToRemove = new ArrayList<>();
    for (String id : iDs) {
      pkToRemove.add(Integer.valueOf(id));
    }
    interestCS.removeInterestsById(pkToRemove, getUserId());
  }

  /**
   * Method removeInterestsById removes Interests object corresponding to given PK
   */
  public void removeICByPK(int pk) throws RemoteException {
    checkServiceInjection();
    interestCS.removeInterestsById(pk);
  }

  public boolean isICExists(String nameIC) throws RemoteException {
    List<Interests> icList = getICByUserId();
    for (Interests ic : icList) {
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
