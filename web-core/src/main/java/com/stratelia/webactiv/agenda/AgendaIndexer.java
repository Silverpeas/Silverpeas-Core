/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
 * @author
 */
public class AgendaIndexer implements ComponentIndexerInterface {

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param context
   * @throws Exception
   * @see
   */
  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {
    AgendaSessionController agenda = new AgendaSessionController(
        mainSessionCtrl, context);

    agenda.indexAll();

  }

}