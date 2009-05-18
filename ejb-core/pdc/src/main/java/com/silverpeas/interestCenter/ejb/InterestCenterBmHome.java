
/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package com.silverpeas.interestCenter.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface InterestCenterBmHome extends EJBHome {

    public InterestCenterBm create() throws RemoteException, CreateException;

}
