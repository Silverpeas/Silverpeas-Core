/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.sharing.control;

import com.silverpeas.sharing.FileSharingRuntimeException;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.services.SharingServiceFactory;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import static com.stratelia.silverpeas.silverpeasinitialize.CallBackManager.*;

public class SharingCallBack implements CallBack {

  public SharingCallBack() {
  }

  /*
   * (non-Javadoc) @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void doInvoke(int action, int iParam, String componentId, Object extraParam) {
    SilverTrace.info("fileSharing", "FileSharingCallback.doInvoke()", "root.MSG_GEN_ENTER_METHOD",
            "action = " + action + ", iParam = " + iParam + ", componentId = " + componentId
            + ", extraParam = " + extraParam.toString());

    if (iParam == -1) {
      SilverTrace.info("fileSharing", "FileSharingCallback.doInvoke()",
              "root.MSG_GEN_PARAM_VALUE", "fileId is null. Callback stopped ! action = " + action
              + ", componentId = " + componentId + ", extraParam = " + extraParam.toString());
      return;
    }

    if (componentId != null) {
      try {
        if (isFileDelete(action)) {
          long fileId = -1L;
          String type = Ticket.FILE_TYPE;
          // extraction fileId
          if (extraParam instanceof AttachmentDetail) {
            fileId = Long.parseLong(((AttachmentDetail) extraParam).getPK().getId());
            type = Ticket.FILE_TYPE;
          } else if (extraParam instanceof Document) {
            fileId = Long.parseLong(((Document) extraParam).getPk().getId());
            type = Ticket.VERSION_TYPE;
          }
          SharingServiceFactory.getSharingTicketService().deleteTicketsForSharedObject(fileId, type);
        }
      } catch (Exception e) {
        throw new FileSharingRuntimeException("FileSharingCallback.doInvoke()",
                SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }

  /*
   * (non-Javadoc) @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  @Override
  public void subscribe() {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.subscribeAction(ACTION_ATTACHMENT_REMOVE, this);
    callBackManager.subscribeAction(ACTION_VERSIONING_REMOVE, this);
  }

  private boolean isFileDelete(int action) {
    return action == ACTION_ATTACHMENT_REMOVE || action == ACTION_VERSIONING_REMOVE;
  }
}