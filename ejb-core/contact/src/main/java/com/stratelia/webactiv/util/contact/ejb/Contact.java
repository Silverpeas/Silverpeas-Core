package com.stratelia.webactiv.util.contact.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.stratelia.webactiv.util.contact.model.CompleteContact;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

public interface Contact extends EJBObject {
  public ContactDetail getDetail() throws RemoteException;

  public CompleteContact getCompleteContact(String modelId)
      throws RemoteException;

  public void setDetail(ContactDetail pubDetail) throws RemoteException;

  public void addFather(NodePK fatherPK) throws RemoteException;

  public void removeFather(NodePK fatherPK) throws RemoteException;

  public void removeAllFather() throws RemoteException;

  public Collection getAllFatherPK() throws RemoteException;

  public void createInfo(String modelId) throws RemoteException;
}