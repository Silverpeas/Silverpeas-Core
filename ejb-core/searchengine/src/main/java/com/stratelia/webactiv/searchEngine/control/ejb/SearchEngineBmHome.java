package com.stratelia.webactiv.searchEngine.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * The home interface for the state-full EJB session SearchEngineBm.
 */
public interface SearchEngineBmHome extends EJBHome
{
  /**
   * Create a SearchEngineBm.
   */
  SearchEngineBm create() throws CreateException, RemoteException;
}
