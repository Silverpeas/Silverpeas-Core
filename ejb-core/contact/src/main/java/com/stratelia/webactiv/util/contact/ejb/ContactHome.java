package com.stratelia.webactiv.util.contact.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;

public interface ContactHome extends EJBHome {
  public Contact create(ContactDetail pubDetail) throws CreateException, RemoteException;
  public Contact findByPrimaryKey(ContactPK pk) throws FinderException, RemoteException;
}