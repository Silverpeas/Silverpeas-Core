package com.silverpeas.myLinks.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface MyLinksBmHome extends EJBHome {
  public MyLinksBm create() throws RemoteException, CreateException;
}
