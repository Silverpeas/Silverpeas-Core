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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.pdcSubscription.util;

import java.rmi.RemoteException;
import java.util.List;

import com.silverpeas.pdcSubscription.PdcSubscriptionRuntimeException;
import com.silverpeas.pdcSubscription.ejb.PdcSubscriptionBm;
import com.silverpeas.pdcSubscription.model.PDCSubscription;

import com.stratelia.silverpeas.classifyEngine.Value;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

/**
 * Utility class. Contains calls of PdcSubscription Ejb
 */
public class PdcSubscriptionUtil {
  private PdcSubscriptionBm scBm;

  private void initEJB() {
    if (scBm == null)
      try {
        scBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PDC_SUBSCRIPTION_EJBHOME, PdcSubscriptionBm.class);
      } catch (Exception e) {
        throw new PdcSubscriptionRuntimeException("PdcSubscriptionSessionController.initEJB()",
            PdcSubscriptionRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
  }

  public PDCSubscription getPDCSubsriptionById(int id) throws RemoteException {
    initEJB();
    return scBm.getPDCSubsriptionById(id);
  }

  public void createPDCSubsription(PDCSubscription subscription) throws RemoteException {
    initEJB();
    scBm.createPDCSubscription(subscription);
  }

  public void updatePDCSubsription(PDCSubscription subscription)
      throws RemoteException {
    initEJB();
    scBm.updatePDCSubscription(subscription);
  }

  public void checkSubscriptions(List<? extends Value> classifyValues, String componentId,
      int silverObjectid) throws RemoteException {
    initEJB();
    scBm.checkSubscriptions(classifyValues, componentId, silverObjectid);
  }

  public void checkAxisOnDelete(int axisId, String axisName)
      throws RemoteException {
    initEJB();
    scBm.checkAxisOnDelete(axisId, axisName);
  }

  public void checkValueOnDelete(int axiId, String axisName, List<String> oldPath,
      List<String> newPath, List<com.stratelia.silverpeas.pdc.model.Value> pathInfo)
      throws RemoteException {
    initEJB();
    scBm.checkValueOnDelete(axiId, axisName, oldPath, newPath, pathInfo);
  }
}
