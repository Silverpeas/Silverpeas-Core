package com.silverpeas.pdcSubscription.util;

import com.silverpeas.pdcSubscription.PdcSubscriptionRuntimeException;
import com.silverpeas.pdcSubscription.ejb.PdcSubscriptionBm;
import com.silverpeas.pdcSubscription.ejb.PdcSubscriptionBmHome;
import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.stratelia.webactiv.util.EJBUtilitaire;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Utility class. Contains calls of PdcSubscription Ejb
 */
public class PdcSubscriptionUtil {
  private PdcSubscriptionBm scBm;

  private void initEJB() {
    if (scBm == null)
      try {
        PdcSubscriptionBmHome icEjbHome = (PdcSubscriptionBmHome) EJBUtilitaire
            .getEJBObjectRef("ejb/pdcSubscription", PdcSubscriptionBmHome.class);
        scBm = icEjbHome.create();
      } catch (Exception e) {
        throw new PdcSubscriptionRuntimeException(
            "PdcSubscriptionSessionController.initEJB()",
            PdcSubscriptionRuntimeException.ERROR,
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
  }

  public PDCSubscription getPDCSubsriptionById(int id) throws RemoteException {
    initEJB();
    return scBm.getPDCSubsriptionById(id);
  }

  public void createPDCSubsription(PDCSubscription subscription)
      throws RemoteException {
    initEJB();
    scBm.createPDCSubscription(subscription);
  }

  public void updatePDCSubsription(PDCSubscription subscription)
      throws RemoteException {
    initEJB();
    scBm.updatePDCSubscription(subscription);
  }

  public void checkSubscriptions(List classifyValues, String componentId,
      int silverObjectid) throws RemoteException {
    initEJB();
    scBm.checkSubscriptions(classifyValues, componentId, silverObjectid);
  }

  public void checkAxisOnDelete(int axisId, String axisName)
      throws RemoteException {
    initEJB();
    scBm.checkAxisOnDelete(axisId, axisName);
  }

  public void checkValueOnDelete(int axiId, String axisName, List oldPath,
      List newPath, List pathInfo) throws RemoteException {
    initEJB();
    scBm.checkValueOnDelete(axiId, axisName, oldPath, newPath, pathInfo);
  }
}
