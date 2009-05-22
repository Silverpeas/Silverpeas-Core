/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

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
 *
 *
 * @author
 */
public class TodoIndexer implements ComponentIndexerInterface
{

    private ToDoSessionController todo = null;

    /**
     * Method declaration
     *
     *
     * @param mainSessionCtrl
     *
     * @throws TodoException
     *
     * @see
     */
    public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws TodoException
    {
        todo = new ToDoSessionController(mainSessionCtrl, context);
        todo.indexAll();
    }

}
