package com.stratelia.silverpeas.comment.ejb;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;


public interface CommentBmHome extends EJBHome
{

    CommentBm create() throws RemoteException, CreateException;
}