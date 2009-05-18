package com.stratelia.webactiv.personalization.control.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface PersonalizationBmHome extends EJBHome {
  
  PersonalizationBm create() throws RemoteException, CreateException;
  
}