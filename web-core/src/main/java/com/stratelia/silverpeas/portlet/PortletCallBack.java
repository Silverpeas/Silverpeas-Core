/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.portlet;

import com.stratelia.silverpeas.portlet.model.PortletColumnRow;
import com.stratelia.silverpeas.portlet.model.PortletRowRow;
import com.stratelia.silverpeas.portlet.model.PortletSchema;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class PortletCallBack extends CallBack
{
    public PortletCallBack()
    {
    }

    public void subscribe()
    {
        CallBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_USER,this);
        CallBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_COMPONENT,this);
        CallBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_SPACE,this);
    }

	public void doInvoke(int action, int iParam, String sParam, Object extraParam)
    {
        PortletSchema	   schema = null;

        try
        {
            schema = new PortletSchema(0);

            SilverTrace.info("portlet", "PortletCallBack.doInvoke()", "root.MSG_GEN_ENTER_METHOD", CallBackManager.getInvokeString(action,iParam,sParam,extraParam));
            if (action == CallBackManager.ACTION_BEFORE_REMOVE_USER)
            {
                schema.portletState.dereferenceUserId(iParam);
            }
            else if (action == CallBackManager.ACTION_BEFORE_REMOVE_COMPONENT)
            {
            	PortletRowRow[] portletRowDeleted = schema.portletRow.dereferenceInstanceId(iParam);
            	for (int i=0; i<portletRowDeleted.length; i++)
                {
            		schema.portletColumn.delete(portletRowDeleted[i].getPortletColumnId());
            		schema.portletState.dereferencePortletRowId(portletRowDeleted[i].getId());
                }
            }
            else if (action == CallBackManager.ACTION_BEFORE_REMOVE_SPACE)
            {
            	PortletColumnRow[] portletColumnDeleted = schema.portletColumn.dereferenceSpaceId(iParam);
            	PortletRowRow[] portletRowDeleted;
            	for (int i=0; i<portletColumnDeleted.length; i++)
                {
            		portletRowDeleted = schema.portletRow.dereferencePortletColumnId(portletColumnDeleted[i].getId());
            		for (int j=0; j<portletRowDeleted.length; j++)
                    {	
            			schema.portletState.dereferencePortletRowId(portletRowDeleted[j].getId());
                    }
                }
            }
            schema.commit();
        }
        catch (Exception e)
        {
            SilverTrace.error("portlet","PortletCallBack.doInvoke()", "portlet.EX_GENERAL", CallBackManager.getInvokeString(action,iParam,sParam,extraParam), e);
            try
            {
                if (schema != null)
                {
                    schema.rollback();
                }
            }
            catch (Exception ex)
            {
                SilverTrace.warn("portlet", "PortletCallBack.doInvoke()", "root.EX_ERR_ROLLBACK", CallBackManager.getInvokeString(action,iParam,sParam,extraParam) , ex);
            }
        }
        finally
        {
            try
            {
                if (schema != null)
                {
                    schema.close();
                }
            }
            catch (Exception e)
            {
                SilverTrace.warn("portlet", "PortletCallBack.doInvoke()", "portlet.EX_CANT_CLOSE_SCHEMA", CallBackManager.getInvokeString(action,iParam,sParam,extraParam) , e);
            }
        }
    }
}
