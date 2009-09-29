package com.stratelia.silverpeas.versioning.ejb;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface VersioningBmHome extends EJBHome {

  VersioningBm create() throws RemoteException, CreateException;
}