package com.stratelia.webactiv.util.questionResult.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface QuestionResultBmHome extends EJBHome {
  
  QuestionResultBm create() throws RemoteException, CreateException;
}