/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.questionContainer.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/*
 * CVS Informations
 * 
 * $Id: QuestionContainerBmHome.java,v 1.1.1.1 2002/08/06 14:47:53 nchaix Exp $
 * 
 * $Log: QuestionContainerBmHome.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.2  2002/01/04 14:22:46  neysseri
 * no message
 *
 */
 
/**
 * Interface declaration
 *
 *
 * @author neysseri
 */
public interface QuestionContainerBmHome extends EJBHome
{

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws CreateException
     * @throws RemoteException
     *
     * @see
     */
    QuestionContainerBm create() throws RemoteException, CreateException;

}
