/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.publication.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/*
 * CVS Informations
 * 
 * $Id: PublicationHome.java,v 1.4 2005/02/23 19:13:55 neysseri Exp $
 * 
 * $Log: PublicationHome.java,v $
 * Revision 1.4  2005/02/23 19:13:55  neysseri
 * intégration Import/Export
 *
 * Revision 1.3.2.1  2005/02/08 18:00:45  tleroi
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/22 15:34:59  neysseri
 * nettoyage eclipse
 *
 * Revision 1.2  2003/08/26 09:39:27  neysseri
 * New method added.
 * This method permits to know if a publication already exists in a given instance.
 * This is a based-name search.
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.4  2002/01/11 12:40:47  neysseri
 * Stabilisation Lot 2 : Exceptions et Silvertrace
 *
 */
 
/**
 * Interface declaration
 *
 *
 * @author
 */
public interface PublicationHome extends EJBHome
{

    /**
     * Method declaration
     *
     *
     * @param pubDetail
     *
     * @return
     *
     * @throws CreateException
     * @throws RemoteException
     *
     * @see
     */
    public Publication create(PublicationDetail pubDetail) throws CreateException, RemoteException;

    /**
     * Method declaration
     *
     *
     * @param pk
     *
     * @return
     *
     * @throws FinderException
     * @throws RemoteException
     *
     * @see
     */
    public Publication findByPrimaryKey(PublicationPK pk) throws FinderException, RemoteException;

	public Publication findByName(PublicationPK pk, String name) throws FinderException, RemoteException;
	
	public Publication findByNameAndNodeId(PublicationPK pk, String name, int nodeId) throws FinderException, RemoteException;
}