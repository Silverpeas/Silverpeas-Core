package com.silverpeas.myLinks.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.silverpeas.myLinks.model.LinkDetail;

/**
 * @author
 */
public interface MyLinksBm extends EJBObject {
  public Collection getAllLinks(String userId) throws RemoteException;

  public Collection getAllLinksByUser(String userId) throws RemoteException;

  public Collection getAllLinksByInstance(String instanceId)
      throws RemoteException;

  public Collection getAllLinksByObject(String instanceId, String objectId)
      throws RemoteException;

  public void createLink(LinkDetail link) throws RemoteException;

  public LinkDetail getLink(String linkId) throws RemoteException;

  public void deleteLinks(String[] links) throws RemoteException;

  public void updateLink(LinkDetail link) throws RemoteException;
}
