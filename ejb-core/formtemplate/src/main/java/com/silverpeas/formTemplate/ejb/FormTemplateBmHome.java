package com.silverpeas.formTemplate.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * Interface declaration
 *
 * @author neysseri
 */
public interface FormTemplateBmHome extends EJBHome
{
	FormTemplateBm create() throws RemoteException, CreateException;
}
