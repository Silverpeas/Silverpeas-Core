/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.notificationManager;

import com.stratelia.silverpeas.notificationManager.model.NotifSchema;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class NotificationManagerCallBack extends CallBack
{
    public NotificationManagerCallBack()
    {
    }

    public void subscribe()
    {
        CallBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_USER,this);
        CallBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_COMPONENT,this);
    }

	public void doInvoke(int action, int iParam, String sParam, Object extraParam)
    {
        NotifSchema	   schema = null;

        try
        {
            schema = new NotifSchema(0);

            SilverTrace.info("notificationManager", "NotificationManagerCallBack.doInvoke()", "root.MSG_GEN_ENTER_METHOD", CallBackManager.getInvokeString(action,iParam,sParam,extraParam));
            if (action == CallBackManager.ACTION_BEFORE_REMOVE_COMPONENT)
            {
                schema.notifPreference.dereferenceComponentInstanceId(iParam);
            }
            else if (action == CallBackManager.ACTION_BEFORE_REMOVE_USER)
            {
                schema.notifDefaultAddress.dereferenceUserId(iParam);
                schema.notifPreference.dereferenceUserId(iParam);
                schema.notifAddress.dereferenceUserId(iParam);
            }
            schema.commit();
        }
        catch (Exception e)
        {
            SilverTrace.error("notificationManager","NotificationManagerCallBack.doInvoke()", "notificationManager.EX_GENERAL", CallBackManager.getInvokeString(action,iParam,sParam,extraParam), e);
            try
            {
                if (schema != null)
                {
                    schema.rollback();
                }
            }
            catch (Exception ex)
            {
                SilverTrace.warn("notificationManager", "NotificationManagerCallBack.doInvoke()", "root.EX_ERR_ROLLBACK", CallBackManager.getInvokeString(action,iParam,sParam,extraParam) , ex);
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
                SilverTrace.warn("notificationManager", "NotificationManagerCallBack.doInvoke()", "notificationManager.EX_CANT_CLOSE_SCHEMA", CallBackManager.getInvokeString(action,iParam,sParam,extraParam) , e);
            }
        }
    }
}
