package com.silverpeas.notation.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface NotationBmHome extends EJBHome
{

    NotationBm create() throws RemoteException, CreateException;
    
}