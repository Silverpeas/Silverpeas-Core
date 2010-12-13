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
 * FLOSS exception.  You should have received a copy of the text describing
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
 */
public class PdcCallBack extends CallBack {

  public PdcCallBack() {
  }

  /*
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  @Override
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

      if (action == CallBackManager.ACTION_BEFORE_REMOVE_USER) {
        pdcBm.deleteManager(String.valueOf(iParam));
      } else if (action == CallBackManager.ACTION_BEFORE_REMOVE_GROUP) {
        pdcBm.deleteGroupManager(String.valueOf(iParam));
      }

    } catch (Exception e) {
      throw new PdcRuntimeException("PdcCallBack.doInvoke()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /*
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  @Override
  public void subscribe() {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_USER, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_GROUP, this);
  }
}