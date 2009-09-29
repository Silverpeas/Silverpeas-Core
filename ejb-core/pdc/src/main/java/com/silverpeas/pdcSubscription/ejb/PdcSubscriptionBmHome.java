/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package com.silverpeas.pdcSubscription.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface PdcSubscriptionBmHome extends EJBHome {

  PdcSubscriptionBm create() throws RemoteException, CreateException;

}
