package com.silverpeas.util.subscribe;

import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBm;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBmHome;
import com.stratelia.webactiv.util.subscribe.model.SubscribeRuntimeException;

/**
 * @author neysseri
 *
 */
public class SubscribeCallBack extends CallBack {
		
	public SubscribeCallBack()
	{
	}
	
	/* 
	 * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int, java.lang.String, java.lang.Object)
	 */
	public void doInvoke(int action, int iParam, String sParam, Object extraParam) { 
		SilverTrace.info("subscribe", "SubscribeCallBack.doInvoke()", "root.MSG_GEN_ENTER_METHOD", "action = "+action+", iParam = "+iParam);
		
		if (iParam == -1)
		{
			SilverTrace.info("subscribe", "SubscribeCallBack.doInvoke()", "root.MSG_GEN_PARAM_VALUE", "userId is null. Callback stopped ! action = "+action+", sParam = "+sParam+", extraParam = "+extraParam.toString());
			return;
		}
		
		try {
			getSubscribeBm().removeUserSubscribes(Integer.toString(iParam));
		} catch (Exception e) {
			throw new SubscribeRuntimeException("SubscribeCallBack.doInvoke()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
	 */
	public void subscribe() {
		CallBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_USER, this);
	}
	
	public SubscribeBm getSubscribeBm() {
  	  SubscribeBm subscribeBm = null;
        try {
      	  SubscribeBmHome subscribeBmHome = (SubscribeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.SUBSCRIBEBM_EJBHOME, SubscribeBmHome.class);
      	  subscribeBm = subscribeBmHome.create();
        } catch (Exception e) {
      	  throw new SubscribeRuntimeException("SubscribeCallBack.getSubscribeBm()",SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
        }
        return subscribeBm;
    }
}