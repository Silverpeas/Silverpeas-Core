/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.clipboard.control;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.owasp.encoder.Encode;
import org.silverpeas.core.index.indexing.model.IndexEntry;

import org.silverpeas.core.notification.user.server.channel.server.SilverMessage;
import org.silverpeas.core.notification.user.server.channel.server.SilverMessageFactory;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.util.ResourceLocator;

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
  private String sessionId = null;

  /**
   * The ClipboardSessionController is built empty and will be later initialized.
   * @param mainSessionCtrl
   * @param context
   */
  public ClipboardSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.clipboard.multilang.clipboard",
        "org.silverpeas.clipboard.settings.clipboardIcons");
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
    String message = Encode.forHtml(request.getParameter("message"));
    StringBuilder str = new StringBuilder();

    if (message != null) {
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
            append(URLUtil.getURL(null, Space, Component)).append(JSPPage).append("';");
        str.append("document.refreshform.Space.value = '").append(Space).append("';");
        str.append("document.refreshform.Component.value = '").append(Component).append("';");
        str.append("document.refreshform.target = '").append(TargetFrame).append("';");
        str.append("document.refreshform.submit();");
      } else if (message.equals("IDLE")) {
        SilverMessage serverMessage =
            SilverMessageFactory.read(
            getUserId(), sessionId);

        if (serverMessage != null) {
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
          org.silverpeas.core.notification.user.server.channel.popup.SilverMessage popupMessage =
              org.silverpeas.core.notification.user.server.channel.popup.SilverMessageFactory
              .read(getUserId());

          if (popupMessage != null) {
            if (popupMessage.getWhat().equals("ALERT")) {
              str.append("msgPopup = SP_openWindow('../..").
                  append(URLUtil.getURL(URLUtil.CMP_POPUP)).
                  append("ReadMessage.jsp?MessageID=").append(popupMessage.getID()).
                  append("','popupmsg").
                  append(new Long(new Date().getTime()).toString()).append(
                  "',500,260,'scrollbars=yes');");
            } else if (popupMessage.getWhat().equals("JAVASCRIPT")) {
              str.append(EncodeHelper.javaStringToJsString(popupMessage.getContent()));
            } else if (popupMessage.getWhat().equals("COMMUNICATION")) {
              request.setAttribute("MessageID", popupMessage.getID());
              str.append("OpenDiscussion('../..").
                  append(URLUtil.getURL(URLUtil.CMP_COMMUNICATIONUSER)).
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
          SilverMessageFactory.del(
              messageId);
        } else if ("POPUP".equals(messageType)) {
          org.silverpeas.core.notification.user.server.channel.popup.SilverMessageFactory.del(
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
    String message = Encode.forHtml(request.getParameter("message"));
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
      SilverTrace.error("clipboardPeas",
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
  public synchronized Collection<IndexEntry> getIndexEntryObjects() throws ClipboardException {
    List<IndexEntry> result = new ArrayList<IndexEntry>();
    for (Transferable clipObject : getClipboardObjects()) {
      if ((clipObject != null)
          && (clipObject.isDataFlavorSupported(ClipboardSelection.IndexFlavor))) {
        try {
          IndexEntry indexEntry;

          indexEntry = (IndexEntry) clipObject.getTransferData(ClipboardSelection.IndexFlavor);
          result.add(indexEntry);
        } catch (Exception e) {
          SilverTrace.error("clipboardPeas", "ClipboardSessionController.getIndexEntryObjects()",
              "root.EX_CLIPBOARD_PASTE_FAILED", "", e);
        }
      }
    }

    return result;
  }

  /**
   * Return the list of object in clipboard.
   * @return the list of object in clipboard.
   * @throws java.rmi.RemoteException
   * @throws javax.naming.NamingException
   * @throws java.sql.SQLException
   */
  public synchronized Collection<ClipboardSelection> getObjects() throws ClipboardException,
      javax.naming.NamingException, java.sql.SQLException {
    return new ArrayList<ClipboardSelection>(getClipboardObjects());
  }

  @Override
  public SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle("org.silverpeas.clipboard.settings.clipboardSettings");
  }

  /**
   * Returns the label of the given domain/space
   * @param spaceId
   * @return the label of the given domain/space
   */
  public String getSpaceLabel(String spaceId) {
    SpaceInst spaceInst = getOrganisationController().getSpaceInstById(spaceId);
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
    ComponentInst componentInst = getOrganisationController().getComponentInst(componentId);
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
