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
 */
public class AttachmentCallBack implements CallBack {

  public AttachmentCallBack() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void doInvoke(int action, int iParam, String componentId, Object extraParam) {
    SilverTrace.info("attachment", "AttachmentCallBack.doInvoke()", "root.MSG_GEN_ENTER_METHOD",
        "action = " + action + ", iParam = " + iParam + ", componentId = " + componentId +
        ", extraParam = " + extraParam.toString());

    if (iParam == -1) {
      SilverTrace.info("attachment", "AttachmentCallBack.doInvoke()", "root.MSG_GEN_PARAM_VALUE",
          "userId is null. Callback stopped ! action = " + action + ", componentId = " +
          componentId + ", extraParam = " + extraParam.toString());
      return;
    }

    if (action == CallBackManager.ACTION_XMLCONTENT_CREATE ||
        action == CallBackManager.ACTION_XMLCONTENT_UPDATE ||
        action == CallBackManager.ACTION_XMLCONTENT_DELETE) {
      Hashtable<String, String> params = (Hashtable<String, String>) extraParam;
      String objectId = params.get("ObjectId");
      String objectLanguage = params.get("ObjectLanguage");
      AttachmentPK pk = new AttachmentPK(objectId, componentId);
      try {
        if (action == CallBackManager.ACTION_XMLCONTENT_CREATE) {
          // Store xmlForm associated to this file
          String xmlFormName = params.get("XMLFormName");
          AttachmentController.addXmlForm(pk, objectLanguage, xmlFormName);
        } else if (action == CallBackManager.ACTION_XMLCONTENT_DELETE) {
          // Remove xmlForm associated to this file
          AttachmentController.addXmlForm(pk, objectLanguage, null);
        }
      } catch (AttachmentException e) {
        SilverTrace.error("attachment", "AttachmentCallBack.doInvoke()",
            "root.MSG_GEN_PARAM_VALUE", e);
      }

      // Force file indexing
      AttachmentController.createIndex(pk);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  @Override
  public void subscribe() {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.subscribeAction(CallBackManager.ACTION_XMLCONTENT_CREATE,
        this);
    callBackManager.subscribeAction(CallBackManager.ACTION_XMLCONTENT_UPDATE,
        this);
    callBackManager.subscribeAction(CallBackManager.ACTION_XMLCONTENT_DELETE,
        this);
  }

}