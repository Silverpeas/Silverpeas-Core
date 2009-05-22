/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.agenda;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.agenda.control.AgendaSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;

/*
 * CVS Informations
 *
 * $Id: AgendaIndexer.java,v 1.2 2004/12/22 15:18:31 neysseri Exp $
 *
 * $Log: AgendaIndexer.java,v $
 * Revision 1.2  2004/12/22 15:18:31  neysseri
 * Possibilité d'indiquer les jours non sélectionnables
 * + nettoyage sources
 * + précompilation jsp
 *
 * Revision 1.1.1.1  2002/08/06 14:47:40  nchaix
 * no message
 *
 * Revision 1.2  2002/01/18 15:00:31  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 *
 */

/**
 * Class declaration
 *
 *
 * @author
 */
public class AgendaIndexer implements ComponentIndexerInterface
{

    /**
     * Method declaration
     *
     *
     * @param mainSessionCtrl
     * @param context
     *
     * @throws Exception
     *
     * @see
     */
    public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws Exception
    {
        AgendaSessionController agenda = new AgendaSessionController(mainSessionCtrl, context);

        agenda.indexAll();

    }

}
