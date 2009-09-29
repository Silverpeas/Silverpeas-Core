package com.stratelia.webactiv.calendar.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface CalendarBmHome extends EJBHome {

  CalendarBm create() throws RemoteException, CreateException;

}