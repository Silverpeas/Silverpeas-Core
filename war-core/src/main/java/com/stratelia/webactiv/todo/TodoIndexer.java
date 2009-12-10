/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.todo;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.todo.control.ToDoSessionController;
import com.stratelia.webactiv.todo.control.TodoException;

/*
 * CVS Informations
 * 
 * $Id: TodoIndexer.java,v 1.2 2004/11/17 19:46:02 neysseri Exp $
 * 
 * $Log: TodoIndexer.java,v $
 * Revision 1.2  2004/11/17 19:46:02  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.3  2002/01/18 14:35:01  lbertin
 * Stabilisation lot 2 : Request routers et sessioncontrollers
 *
 * Revision 1.2  2002/01/02 09:54:58  mmarengo
 * Stabilisation Lot 2
 *
 */

/**
 * Class declaration
 * @author
 */
public class TodoIndexer implements ComponentIndexerInterface {

  private ToDoSessionController todo = null;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @throws TodoException
   * @see
   */
  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws TodoException {
    todo = new ToDoSessionController(mainSessionCtrl, context);
    todo.indexAll();
  }

}
