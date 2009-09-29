package com.silverpeas.pdc.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * Interface declaration
 * 
 * @author neysseri
 */
public interface PdcBmHome extends EJBHome {
  PdcBm create() throws RemoteException, CreateException;
}
