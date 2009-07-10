package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.Actions;

/**
 * Class implementing the representation of the &lt;actions&gt; element of a Process Model.
**/
public class ActionsImpl implements Serializable, Actions 
{
    //private Hashtable actionList;
	private List actionList;

	/**
	 * Constructor
	 */
    public ActionsImpl() 
	{
        actionList = new ArrayList();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Actions#addAction(com.silverpeas.workflow.api.model.Action)
     */
    public void addAction(Action action)
    {
        actionList.add(action);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Actions#createAction()
     */
    public Action createAction()
    {
        return new ActionImpl();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Actions#getAction(java.lang.String)
     */
    public Action getAction(String name) throws WorkflowException {
        boolean find = false;
        Action action = null;
        
        for (int a=0; !find && a<actionList.size(); a++)
        {
            action = (Action) actionList.get(a);
            if (action != null && action.getName().equals(name))
                find = true;
        }
        
        if (find)
            return action;
        else
            throw new WorkflowException(
                    "ActionsImpl.getAction(String)",
                    "WorkflowEngine.EX_ERR_ACTION_NOT_FOUND_IN_MODEL",
                    name);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Actions#getActions()
     */
    public Action[] getActions() {
        if (actionList == null)
            return null;

        return (Action[]) actionList.toArray(new ActionImpl[0]);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Actions#iterateAction()
     */
    public Iterator iterateAction()
    {
        return actionList.iterator();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Actions#removeAction(java.lang.String)
     */
    public void removeAction(String strActionName) throws WorkflowException
    {
        Action action = createAction();
        
        action.setName(strActionName);
        
        if ( actionList == null )
            return;
        
        if ( !actionList.remove(action) )
            throw new WorkflowException("ActionsImpl.removeAction()", //$NON-NLS-1$
                                        "workflowEngine.EX_ERR_ACTION_NOT_FOUND_IN_MODEL",               // $NON-NLS-1$
                                        strActionName == null
                                            ? "<null>"  //$NON-NLS-1$
                                            : strActionName );
    }
}