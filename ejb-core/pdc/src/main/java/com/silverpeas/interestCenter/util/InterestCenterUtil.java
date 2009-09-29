/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.silverpeas.interestCenter.util;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import com.silverpeas.interestCenter.InterestCenterRuntimeException;
import com.silverpeas.interestCenter.ejb.InterestCenterBm;
import com.silverpeas.interestCenter.ejb.InterestCenterBmHome;
import com.silverpeas.interestCenter.model.InterestCenter;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

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
        InterestCenterBmHome icEjbHome = (InterestCenterBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.INTEREST_CENTER_EJBHOME,
                InterestCenterBmHome.class);
        icEjb = icEjbHome.create();
      } catch (Exception e) {
        throw new InterestCenterRuntimeException(
            "InterestCenterUtil.initEJB()", "root.EX_CANT_GET_REMOTE_OBJECT",
            "", e);
      }
    }
  }

  /**
   * Method getICByUserId
   * 
   * returns ArrayList of all InterestCenter objects for user given by userId
   * 
   */
  public ArrayList getICByUserId(int userId) throws RemoteException {
    initEJB();
    return icEjb.getICByUserID(userId);
  }

  /**
   * Method getICByPK
   * 
   * returns Interest Center given by id
   * 
   */
  public InterestCenter getICByID(int id) throws RemoteException {
    initEJB();
    return icEjb.getICByID(id);
  }

  /**
   * Method isICExists
   * 
   * returns true if InterstCenter with given name is already exists, false in
   * other case
   * 
   */

  public int isICExists(String nameIC, int userId) throws RemoteException {
    ArrayList icList;
    InterestCenter ic;

    icList = getICByUserId(userId);
    Iterator it = icList.iterator();
    while (it.hasNext()) {
      ic = (InterestCenter) it.next();
      if (nameIC.equals(ic.getName())) {
        return ic.getId();
      }
    }

    return -1;

  }

  /**
   * Method createIC
   * 
   * creates new InterestCenter
   * 
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
