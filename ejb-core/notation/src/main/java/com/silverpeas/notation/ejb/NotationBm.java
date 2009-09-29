package com.silverpeas.notation.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.silverpeas.notation.model.NotationDetail;
import com.silverpeas.notation.model.NotationPK;

public interface NotationBm extends EJBObject {

  public void updateNotation(NotationPK pk, int note) throws RemoteException;

  public void deleteNotation(NotationPK pk) throws RemoteException;

  public NotationDetail getNotation(NotationPK pk) throws RemoteException;

  public int countNotations(NotationPK pk) throws RemoteException;

  public boolean hasUserNotation(NotationPK pk) throws RemoteException;

  public Collection getBestNotations(NotationPK pk, int notationsCount)
      throws RemoteException;

  public Collection getBestNotations(Collection pks, int notationsCount)
      throws RemoteException;

}