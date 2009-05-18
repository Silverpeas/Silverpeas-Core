package com.silverpeas.tagcloud.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;


public interface TagCloudBmHome extends EJBHome
{

    TagCloudBm create() throws RemoteException, CreateException;
    
}