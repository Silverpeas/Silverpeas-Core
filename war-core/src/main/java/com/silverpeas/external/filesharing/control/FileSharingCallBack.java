package com.silverpeas.external.filesharing.control;

import com.silverpeas.external.filesharing.model.FileSharingInterface;
import com.silverpeas.external.filesharing.model.FileSharingInterfaceImpl;
import com.silverpeas.external.filesharing.model.FileSharingRuntimeException;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class FileSharingCallBack extends CallBack {

  public FileSharingCallBack() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int,
   * int, java.lang.String, java.lang.Object)
   */
  public void doInvoke(int action, int iParam, String componentId,
      Object extraParam) {
    SilverTrace.info("fileSharing", "FileSharingCallback.doInvoke()",
        "root.MSG_GEN_ENTER_METHOD", "action = " + action + ", iParam = "
            + iParam + ", componentId = " + componentId + ", extraParam = "
            + extraParam.toString());

    if (iParam == -1) {
      SilverTrace.info("fileSharing", "FileSharingCallback.doInvoke()",
          "root.MSG_GEN_PARAM_VALUE",
          "fileId is null. Callback stopped ! action = " + action
              + ", componentId = " + componentId + ", extraParam = "
              + extraParam.toString());
      return;
    }

    if (componentId != null) {
      try {
        if (isFileDelete(action)) {
          String fileId = null;
          boolean versioning = false;
          // extraction fileId
          if (extraParam instanceof AttachmentDetail) {
            AttachmentDetail attachment = (AttachmentDetail) extraParam;
            fileId = attachment.getPK().getId();
            versioning = false;
          } else if (extraParam instanceof Document) {
            Document document = (Document) extraParam;
            fileId = document.getPk().getId();
            versioning = true;
          }
          getFileSharingInterface().deleteTicketsByFile(fileId, versioning);
        }
      } catch (Exception e) {
        throw new FileSharingRuntimeException("FileSharingCallback.doInvoke()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  public void subscribe() {
    CallBackManager.subscribeAction(CallBackManager.ACTION_ATTACHMENT_REMOVE,
        this);
    CallBackManager.subscribeAction(CallBackManager.ACTION_VERSIONING_REMOVE,
        this);
  }

  private boolean isFileDelete(int action) {
    return (action == CallBackManager.ACTION_ATTACHMENT_REMOVE || action == CallBackManager.ACTION_VERSIONING_REMOVE);
  }

  private FileSharingInterface getFileSharingInterface() {
    FileSharingInterface fileSharingInterface = null;
    try {
      fileSharingInterface = new FileSharingInterfaceImpl();
    } catch (Exception e) {
      throw new FileSharingRuntimeException(
          "FileSharingSessionController.getFileSharingInterface()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return fileSharingInterface;
  }

}