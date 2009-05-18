package com.silverpeas.thesaurus.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ThesaurusBmHome extends EJBHome {

	ThesaurusBm create() throws RemoteException, CreateException;

}