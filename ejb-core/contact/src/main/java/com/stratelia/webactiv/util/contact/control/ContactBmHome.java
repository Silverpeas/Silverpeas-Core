package com.stratelia.webactiv.util.contact.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface ContactBmHome extends EJBHome {

  ContactBm create() throws RemoteException, CreateException;
}