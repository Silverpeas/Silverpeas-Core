package com.stratelia.webactiv.util.readingControl.control;

import javax.ejb.*;
import java.util.Collection;
import java.util.Hashtable;
import java.rmi.RemoteException;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public interface ReadingControlBm extends EJBObject {

  public void addReadingControls(Collection userIds, PublicationPK pubPK) throws RemoteException;
  
  public Hashtable getReadingStates(PublicationPK pubPK) throws RemoteException;
  
  public void removeReadingControl(Collection userIds, PublicationPK pubPK) throws RemoteException;
  
  public void removeReadingControlByUser(String userId) throws RemoteException;
  
  public void removeReadingControlByPublication(PublicationPK pubPK) throws RemoteException;
}