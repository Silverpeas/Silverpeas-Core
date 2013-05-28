/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.attachment;

import java.util.Map;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import static com.stratelia.silverpeas.silverpeasinitialize.CallBackManager.*;

public class AttachmentCallback implements CallBack {

  public AttachmentCallback() {
  }

  @Override
  public void doInvoke(int action, int iParam, String componentId, Object extraParam) {
    SilverTrace.info("attachment", "AttachmentCallBack.doInvoke()", "root.MSG_GEN_ENTER_METHOD",
        "action = " + action + ", iParam = " + iParam + ", componentId = " + componentId
        + ", extraParam = " + extraParam.toString());

    if (iParam == -1) {
      SilverTrace.info("attachment", "AttachmentCallBack.doInvoke()", "root.MSG_GEN_PARAM_VALUE",
          "userId is null. Callback stopped ! action = " + action + ", componentId = " + componentId
          + ", extraParam = " + extraParam.toString());
      return;
    }

    if (action == ACTION_XMLCONTENT_CREATE || action == ACTION_XMLCONTENT_UPDATE || action
        == ACTION_XMLCONTENT_DELETE) {
      Map<String, String> params = (Map<String, String>) extraParam;
      if ("Attachment".equalsIgnoreCase(params.get("ObjectType"))) {
        String objectLanguage = params.get("ObjectLanguage");
        String objectId = params.get("ObjectId");
        SimpleDocumentPK pk;
        if (StringUtil.isLong(objectId)) {
          long oldSilverpeasId = Long.parseLong(objectId);
          pk = new SimpleDocumentPK(null, componentId);
          pk.setOldSilverpeasId(oldSilverpeasId);
        } else {
          pk = new SimpleDocumentPK(objectId, componentId);
        }
        SimpleDocument doc = AttachmentServiceFactory.getAttachmentService().searchDocumentById(pk,
            objectLanguage);
        pk = doc.getPk();
        try {
          String xmlFormName = null;
          if (action == ACTION_XMLCONTENT_CREATE || action == ACTION_XMLCONTENT_UPDATE) {
            xmlFormName = params.get("XMLFormName");
          }
          doc.setXmlFormId(xmlFormName);
          AttachmentServiceFactory.getAttachmentService().addXmlForm(pk, objectLanguage, xmlFormName);
        } catch (AttachmentException e) {
          SilverTrace.error("attachment", "AttachmentCallBack.doInvoke()",
              "root.MSG_GEN_PARAM_VALUE", e);
        }
        AttachmentServiceFactory.getAttachmentService().createIndex(doc);

      }
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  @Override
  public void subscribe() {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.subscribeAction(ACTION_XMLCONTENT_CREATE, this);
    callBackManager.subscribeAction(ACTION_XMLCONTENT_UPDATE, this);
    callBackManager.subscribeAction(ACTION_XMLCONTENT_DELETE, this);
  }
}
