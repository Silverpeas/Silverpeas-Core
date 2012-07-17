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

package com.silverpeas.subscribe;

import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author neysseri
 */
public class SubscriptionCallBack implements CallBack {

  public SubscriptionCallBack() {
  }

  /*
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void doInvoke(int action, int iParam, String sParam, Object extraParam) {
    SilverTrace.info("subscribe", "SubscriptionCallBack.doInvoke()", "root.MSG_GEN_ENTER_METHOD",
        "action = " + action + ", iParam = " + iParam);
    if (iParam == -1) {
      SilverTrace.info("subscribe", "SubscriptionCallBack.doInvoke()", "root.MSG_GEN_PARAM_VALUE",
          "userId is null. Callback stopped ! action = " + action + ", sParam = " + sParam
          + ", extraParam = " + extraParam.toString());
      return;
    }
    getSubscribeBm().unsubscribe(Integer.toString(iParam));
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  @Override
  public void subscribe() {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_USER, this);
  }

  public SubscriptionService getSubscribeBm() {
    return SubscriptionServiceFactory.getFactory().getSubscribeService();
  }
}