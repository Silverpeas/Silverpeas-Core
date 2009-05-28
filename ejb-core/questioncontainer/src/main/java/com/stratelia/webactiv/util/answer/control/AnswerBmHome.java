/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.answer.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * Interface declaration
 *
 * @author neysseri
 */
public interface AnswerBmHome extends EJBHome
{

	AnswerBm create() throws RemoteException, CreateException;
}
