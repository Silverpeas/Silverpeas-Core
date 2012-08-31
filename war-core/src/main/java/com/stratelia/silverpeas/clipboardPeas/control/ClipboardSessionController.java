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

package com.stratelia.silverpeas.clipboardPeas.control;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.silverpeas.notificationserver.channel.popup.SilverMessage;
import com.stratelia.silverpeas.notificationserver.channel.popup.SilverMessageFactory;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.search.indexEngine.model.IndexEntry;

/**
 * A servlet ClipboardSessionControler acts as a proxy for a ClipboardBm EJB.
 */
public class ClipboardSessionController extends AbstractComponentSessionController {

  /*
   * The Web'Activ context
   */
  private int m_counter = 0;

  /*
   * Attributes from the caller component (paste operation)
   */
  private String m_CallerRooterName;
  private String m_CallerJSPPage;
  private String m_CallerTargetFrame;
  /**
   * The bundle containing all the settings.
   */
  private ResourceLocator settings = null;
  private String sessionId = null;

  /**
   * The ClipboardSessionController is built empty and will be later initialized.
   * @param mainSessionCtrl
   * @param context
   */
  public ClipboardSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "com.stratelia.webactiv.clipboard.multilang.clipboard",
        "com.stratelia.webactiv.clipboard.settings.clipboardIcons");
    sessionId = mainSessionCtrl.getSessionId();
  }

  /**
   * Method getCounter
   * @return
   * @see
   */
  public int getCounter() {
    return m_counter;
  }

  /**
   * Method incCounter
   * @param inc
   * @see
   */
  public void incCounter(int inc) {
    m_counter += inc;
  }

  /**
   * Method doIdle
   * @param nbinc
   * @see
   */
  public void doIdle(int nbinc) {
    incCounter(nbinc);
  }

  /**
   * Method getJavaScriptTask
   * @param request
   * @return
   * @see
   */
  public String getHF_JavaScriptTask(HttpServletRequest request) {
    String message = request.getParameter("message");
    StringBuilder str = new StringBuilder();

    if (message != null) {
      SilverTrace.info("clipboardPeas",
          "ClipboardSessionController.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", " message = " + message);

      if (message.equals("SHOWCLIPBOARD")) {
        // portage netscape
        str
            .append(
            "top.ClipboardWindow = window.open('../../Rclipboard/jsp/clipboard.jsp','Clipboard','width=500,height=350,alwaysRaised');");
        str.append("top.ClipboardWindow.focus();");
      } else if (message.equals("REFRESHCLIPBOARD")) {
        // portage netscape
        str.append("if(top.ClipboardWindow!=null){");
        str.append("if (!top.ClipboardWindow.closed) {");
        str
            .append(
            "top.ClipboardWindow = window.open('../../Rclipboard/jsp/clipboardRefresh.jsp','Clipboard','width=350,height=300,alwaysRaised');");
        str.append("}");
        str.append("}");

      } else if (message.equals("REFRESH")) {
        // parameters for refresh mecanisme
        String Space = request.getParameter("SpaceFrom");
        String Component = request.getParameter("ComponentFrom");
        String TargetFrame = request.getParameter("TargetFrame");
        String JSPPage = request.getParameter("JSPPage");

        str.append("document.refreshform.action = '../..").
            append(URLManager.getURL(null, Space, Component)).append(JSPPage).append("';");
        str.append("document.refreshform.Space.value = '").append(Space).append("';");
        str.append("document.refreshform.Component.value = '").append(Component).append("';");
        str.append("document.refreshform.target = '").append(TargetFrame).append("';");
        str.append("document.refreshform.submit();");
      } else if (message.equals("IDLE")) {
        com.stratelia.silverpeas.notificationserver.channel.server.SilverMessage serverMessage =
            com.stratelia.silverpeas.notificationserver.channel.server.SilverMessageFactory.read(
            getUserId(), sessionId);

        if (serverMessage != null) {
          SilverTrace.info("clipboardPeas",
              "ClipboardSessionController.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", " serverMessage = " + serverMessage);
          if (serverMessage.getWhat().equals("ALERT")) {
            str.append("alert ('");
            str.append(EncodeHelper.javaStringToJsString(serverMessage.getContent()));
            str.append("');");
            str
                .append(
                    "self.location.href = '../../Rclipboard/jsp/Idle.jsp?message=DELMSG&messageTYPE=SERVER&messageID=")
                .
                append(serverMessage.getID()).append("';");
          } else if (serverMessage.getWhat().equals("JAVASCRIPT")) {
            str.append(EncodeHelper.javaStringToJsString(serverMessage.getContent()));
          }
        } else {
          SilverMessage popupMessage = SilverMessageFactory.read(getUserId());

          if (popupMessage != null) {
            SilverTrace.info("clipboardPeas",
                "ClipboardSessionController.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", " popupMessage = "
                + popupMessage.getWhat());
            if (popupMessage.getWhat().equals("ALERT")) {
              SilverTrace.info("clipboardPeas",
                  "ClipboardSessionController.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE",
                  " URLManager.getURL(URLManager.CMP_POPUP) = "
                  + URLManager.getURL(URLManager.CMP_POPUP));
              SilverTrace.info("clipboardPeas",
                  "ClipboardSessionController.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", " MessageID = "
                  + popupMessage.getID());
              str.append("msgPopup = SP_openWindow('../..").
                  append(URLManager.getURL(URLManager.CMP_POPUP)).
                  append("ReadMessage.jsp?MessageID=").append(popupMessage.getID()).
                  append("','popupmsg").
                  append(new Long(new Date().getTime()).toString()).append(
                  "',500,260,'scrollbars=yes');");
            } else if (popupMessage.getWhat().equals("JAVASCRIPT")) {
              str.append(EncodeHelper.javaStringToJsString(popupMessage.getContent()));
            } else if (popupMessage.getWhat().equals("COMMUNICATION")) {
              SilverTrace.info("clipboardPeas",
                  "ClipboardSessionController.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE",
                  " URLManager.getURL(URLManager.CMP_COMMUNICATIONUSER) = "
                  + URLManager.getURL(URLManager.CMP_COMMUNICATIONUSER));
              SilverTrace.info("clipboardPeas",
                  "ClipboardSessionController.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", " userId = "
                  + popupMessage.getSenderId());
              request.setAttribute("MessageID", popupMessage.getID());
              str.append("OpenDiscussion('../..").
                  append(URLManager.getURL(URLManager.CMP_COMMUNICATIONUSER)).
                  append("OpenDiscussion?userId=").append(popupMessage.getSenderId()).
                  append("&MessageID=").append(popupMessage.getID()).append("','popupDiscussion").
                  append(popupMessage.getSenderId()).append(
                  "',650,400,'menubar=no,scrollbars=no,statusbar=no');");
            }
          }
        }
      } else if ("DELMSG".equals(message)) {
        String messageId = request.getParameter("messageID");
        String messageType = request.getParameter("messageTYPE");
        if ("SERVER".equals(messageType)) {
          com.stratelia.silverpeas.notificationserver.channel.server.SilverMessageFactory.del(
              messageId);
        } else if ("POPUP".equals(messageType)) {
          com.stratelia.silverpeas.notificationserver.channel.popup.SilverMessageFactory.del(
              messageId);
        }
      }
    }
    return str.toString();
  }

  /**
   * Method getJavaScriptTask
   * @param request
   * @return
   * @see
   */
  public String getHF_HTMLForm(HttpServletRequest request) {
    String message = request.getParameter("message");
    StringBuilder str = new StringBuilder("");
    if ("REFRESH".equals(message)) {
      str.append("<form name='refreshform' action='' method='post' target='MyMain'>");
      str.append("<input type='hidden' name='Space'>");
      str.append("<input type='hidden' name='Component'>");
      str.append("</form>");
    }
    return str.toString();
  }

  public synchronized String getMessageError() {
    String errorMessage = null;
    try {
      errorMessage = getClipboardErrorMessage();
      if (errorMessage != null) {
        errorMessage = getString(errorMessage);
        Exception exc = getClipboardExceptionError();
        if (exc != null) {
          errorMessage += exc.getMessage();
        }
      }
    } catch (Exception e) {
      SilverTrace.info("clipboardPeas",
          "ClipboardSessionController.getMessageError()",
          "clipboardPeas.EX_CANT_GET_MESSAGE", "", e);
    }
    return errorMessage;
  }

  /**
   *Return the list of object (IndexEntry format) in clipboard.
   * @return the list of object (IndexEntry format) in clipboard.
   * @throws java.rmi.RemoteException
   */
  public synchronized Collection<IndexEntry> getIndexEntryObjects() throws java.rmi.RemoteException {
    SilverTrace.info("clipboardPeas",
        "ClipboardSessionController.getStrateliaReferenceObjects()",
        "root.MSG_GEN_ENTER_METHOD");
    List<IndexEntry> result = new ArrayList<IndexEntry>();
    for (Transferable clipObject : getClipboardObjects()) {
      if ((clipObject != null)
          && (clipObject.isDataFlavorSupported(ClipboardSelection.IndexFlavor))) {
        try {
          IndexEntry indexEntry;

          indexEntry = (IndexEntry) clipObject.getTransferData(ClipboardSelection.IndexFlavor);
          result.add(indexEntry);
        } catch (Exception e) {
          SilverTrace.error("clipboardPeas",
              "ClipboardSessionController.getIndexEntryObjects()",
              "root.EX_CLIPBOARD_PASTE_FAILED", "", e);
        }
      }
    }
    SilverTrace.info("clipboardPeas",
        "ClipboardSessionController.getStrateliaReferenceObjects()",
        "root.MSG_GEN_EXIT_METHOD");
    return result;
  }

  /**
   * Return the list of object in clipboard.
   * @return the list of object in clipboard.
   * @throws java.rmi.RemoteException
   * @throws javax.naming.NamingException
   * @throws java.sql.SQLException
   */
  public synchronized Collection<ClipboardSelection> getObjects() throws java.rmi.RemoteException,
      javax.naming.NamingException, java.sql.SQLException {
    List<ClipboardSelection> result = new ArrayList<ClipboardSelection>(getClipboardObjects());
    return result;
  }

  @Override
  public ResourceLocator getSettings() {
    if (settings == null) {
      settings = new ResourceLocator(
          "com.stratelia.webactiv.clipboard.settings.clipboardSettings", "");
    }
    return settings;
  }

  /**
   * Returns the label of the given domain/space
   * @param spaceId
   * @return the label of the given domain/space
   */
  public String getSpaceLabel(String spaceId) {
    SpaceInst spaceInst = getOrganizationController().getSpaceInstById(spaceId);
    if (spaceInst != null) {
      return spaceInst.getName();
    }
    return spaceId;
  }

  /**
   * Returns the label of the given component
   * @param componentId
   * @return
   */
  public String getComponentLabel(String componentId) {
    ComponentInst componentInst = getOrganizationController().getComponentInst(componentId);
    if (componentInst != null) {
      if (componentInst.getLabel().length() > 0) {
        return componentInst.getLabel();
      }
      return componentInst.getName();
    }
    SilverTrace.error("clipboardPeas", "ClipboardSessionController.getComponentLabel()",
        "clipboardPeas.EX_CANT_GET_COMPO_LABEL");
    return componentId;
  }

  /**
   * @param rooterName
   */
  public void setComponentRooterName(String rooterName) {
    m_CallerRooterName = rooterName;
  }

  /**
   *
   */
  public void setSpaceId(String spaceId) {
    this.context.setCurrentSpaceId(spaceId);
  }

  /**
   *
   */
  public void setComponentId(String componentId) {
    this.context.setCurrentComponentId(componentId);
  }

  /**
   * @param JSPPage
   */
  public void setJSPPage(String JSPPage) {
    m_CallerJSPPage = JSPPage;
  }

  /**
   * @param TargetFrame
   */
  public void setTargetFrame(String TargetFrame) {
    m_CallerTargetFrame = TargetFrame;
  }

  /**
   * @return
   */
  public String getComponentRooterName() {
    return m_CallerRooterName;
  }

  /**
   * @return
   */
  public String getJSPPage() {
    return m_CallerJSPPage;
  }

  /**
   * @return
   */
  public String getTargetFrame() {
    return m_CallerTargetFrame;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIntervalInSec() {
    return getSettings().getString("IntervalInSec");
  }
}
