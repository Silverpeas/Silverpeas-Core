package com.stratelia.webactiv.util.readingControl.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface ReadingControlBmHome extends EJBHome {
  
  ReadingControlBm create() throws RemoteException, CreateException;
}