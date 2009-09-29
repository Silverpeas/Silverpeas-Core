package com.silverpeas.pdc;

import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.PdcRuntimeException;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * @author neysseri
 * 
 */
public class PdcCallBack extends CallBack {

  public PdcCallBack() {
  }

  /*
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int,
   * int, java.lang.String, java.lang.Object)
   */
  public void doInvoke(int action, int iParam, String sParam, Object extraParam) {
    SilverTrace.info("Pdc", "PdcCallBack.doInvoke()",
        "root.MSG_GEN_ENTER_METHOD", "action = " + action + ", iParam = "
            + iParam);

    if (iParam == -1) {
      SilverTrace.info("Pdc", "PdcCallBack.doInvoke()",
          "root.MSG_GEN_PARAM_VALUE",
          "userId or groupId is null. Callback stopped ! action = " + action
              + ", sParam = " + sParam + ", extraParam = "
              + extraParam.toString());
      return;
    }

    try {
      PdcBm pdcBm = new PdcBmImpl();

      if (action == CallBackManager.ACTION_BEFORE_REMOVE_USER)
        pdcBm.deleteManager(Integer.toString(iParam));
      else if (action == CallBackManager.ACTION_BEFORE_REMOVE_GROUP)
        pdcBm.deleteGroupManager(Integer.toString(iParam));

    } catch (Exception e) {
      throw new PdcRuntimeException("PdcCallBack.doInvoke()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /*
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  public void subscribe() {
    CallBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_USER,
        this);
    CallBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_GROUP,
        this);
  }
}