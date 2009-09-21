package com.stratelia.webactiv.util.attachment;

import java.util.Hashtable;

import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;

/**
 * @author neysseri
 *
 */
public class AttachmentCallBack extends CallBack {
	
	public AttachmentCallBack()
	{
	}
	
	/* (non-Javadoc)
	 * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int, java.lang.String, java.lang.Object)
	 */
	public void doInvoke(int action, int iParam, String componentId, Object extraParam) { 
		SilverTrace.info("attachment", "AttachmentCallBack.doInvoke()", "root.MSG_GEN_ENTER_METHOD", "action = "+action+", iParam = "+iParam+", componentId = "+componentId+", extraParam = "+extraParam.toString());
		
		if (iParam == -1)
		{
			SilverTrace.info("attachment", "AttachmentCallBack.doInvoke()", "root.MSG_GEN_PARAM_VALUE", "userId is null. Callback stopped ! action = "+action+", componentId = "+componentId+", extraParam = "+extraParam.toString());
			return;
		}

		if (action == CallBackManager.ACTION_XMLCONTENT_CREATE || action == CallBackManager.ACTION_XMLCONTENT_UPDATE || action == CallBackManager.ACTION_XMLCONTENT_DELETE)
		{
			Hashtable<String, String> params = (Hashtable<String, String>) extraParam;
			String objectId = params.get("ObjectId");
			String objectLanguage = params.get("ObjectLanguage");
			AttachmentPK pk = new AttachmentPK(objectId, componentId);
			try {
				if (action == CallBackManager.ACTION_XMLCONTENT_CREATE)
				{
					//Store xmlForm associated to this file
					String xmlFormName = params.get("XMLFormName");
					AttachmentController.addXmlForm(pk, objectLanguage, xmlFormName);
				}
				else if (action == CallBackManager.ACTION_XMLCONTENT_DELETE)
				{
					//Remove xmlForm associated to this file
					AttachmentController.addXmlForm(pk, objectLanguage, null);
				}
			} catch (AttachmentException e) {
				SilverTrace.error("attachment", "AttachmentCallBack.doInvoke()", "root.MSG_GEN_PARAM_VALUE", e);
			}
			
			//Force file indexing
			AttachmentController.createIndex(pk);
		}
	}

	/* (non-Javadoc)
	 * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
	 */
	public void subscribe() {
		CallBackManager.subscribeAction(CallBackManager.ACTION_XMLCONTENT_CREATE, this);
		CallBackManager.subscribeAction(CallBackManager.ACTION_XMLCONTENT_UPDATE, this);
		CallBackManager.subscribeAction(CallBackManager.ACTION_XMLCONTENT_DELETE, this);
	}	
	
}