/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.clipboard.control;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.notification.user.server.channel.popup.PopupMessageService;
import org.silverpeas.core.notification.user.server.channel.popup.PopupMsg;
import org.silverpeas.core.notification.user.server.channel.server.ServerMessageService;
import org.silverpeas.core.notification.user.server.channel.server.ServerMsg;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.http.HttpServletRequest;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;

/**
 * A servlet ClipboardSessionController acts as a proxy for a Clipboard Service.
 */
public class ClipboardSessionController extends AbstractComponentSessionController {

  /*
   * The context
   */
  private int counter = 0;

  /*
   * Attributes from the caller component (paste operation)
   */
  private String callerRooterName;
  private String callerJSPPage;
  private String callerTargetFrame;
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
    return counter;
  }

  /**
   * Method incCounter
   * @param inc
   * @see
   */
  public void incCounter(int inc) {
    counter += inc;
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
  public String getJavaScriptTaskForHiddenFrame(HttpServletRequest request) {
    String message = Encode.forHtml(request.getParameter("message"));
    String js = "";

    if ("SHOWCLIPBOARD".equals(message)) {
      js = getJSForShowClipboard();
    } else if ("REFRESHCLIPBOARD".equals(message)) {
      // portage netscape
      js = getJSForRefreshClipboard();
    } else if ("REFRESH".equals(message)) {
      js = getJSForRefresh(request);
    } else if ("IDLE".equals(message)) {
      js = getJSForIdle();
    } else if ("DELMSG".equals(message)) {
      String messageId = request.getParameter("messageID");
      String messageType = request.getParameter("messageTYPE");
      if (StringUtil.isDefined(messageId)) {
        if ("SERVER".equals(messageType)) {
          ServerMessageService.get().deleteById(messageId);
        } else if ("POPUP".equals(messageType)) {
          PopupMessageService.get().deleteById(messageId);
        }
      }
    }
    return js;
  }

  /**
   * Method getJavaScriptTask
   * @param request
   * @return
   * @see
   */
  public String getHTMLFormForHiddenFrame(HttpServletRequest request) {
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
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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
          SilverLogger.getLogger(this).error(e.getMessage(), e);
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
  public synchronized Collection<ClipboardSelection> getObjects() throws ClipboardException {
    return new ArrayList<>(getClipboardObjects());
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
    SilverLogger.getLogger(this).error(failureOnGetting("component instance", componentId));
    return componentId;
  }

  /**
   * @param rooterName
   */
  public void setComponentRooterName(String rooterName) {
    callerRooterName = rooterName;
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
   * @param jspPage
   */
  public void setJSPPage(String jspPage) {
    callerJSPPage = jspPage;
  }

  /**
   * @param targetFrame
   */
  public void setTargetFrame(String targetFrame) {
    callerTargetFrame = targetFrame;
  }

  /**
   * @return
   */
  public String getComponentRooterName() {
    return callerRooterName;
  }

  /**
   * @return
   */
  public String getJSPPage() {
    return callerJSPPage;
  }

  /**
   * @return
   */
  public String getTargetFrame() {
    return callerTargetFrame;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIntervalInSec() {
    return getSettings().getString("IntervalInSec");
  }

  private String getJSForIdle() {
    StringBuilder str = new StringBuilder();
    ServerMsg serverMsg = ServerMessageService.get().read(getUserId(), sessionId);

    if (serverMsg != null) {
      if ("ALERT".equals(serverMsg.getWhat())) {
        str.append("alert ('");
        str.append(WebEncodeHelper.javaStringToJsString(serverMsg.getContent()));
        str.append("');");
        str.append(
            "self.location.href = '../../Rclipboard/jsp/Idle" +
                ".jsp?message=DELMSG&messageTYPE=SERVER&messageID=")
            .
                append(serverMsg.getID()).append("';");
      } else if ("JAVASCRIPT".equals(serverMsg.getWhat())) {
        str.append(WebEncodeHelper.javaStringToJsString(serverMsg.getContent()));
      }
    } else {
      PopupMsg popupMessage = PopupMessageService.get().read(getUserId());

      if (popupMessage != null) {
        if ("ALERT".equals(popupMessage.getWhat())) {
          str.append("msgPopup = SP_openWindow('../..").
              append(URLUtil.getURL(URLUtil.CMP_POPUP)).
              append("ReadMessage.jsp?MessageID=").append(popupMessage.getID()).
              append("','popupmsg").
              append(Long.toString(new Date().getTime())).append("',500,260,'scrollbars=yes');");
        } else if ("JAVASCRIPT".equals(popupMessage.getWhat())) {
          str.append(WebEncodeHelper.javaStringToJsString(popupMessage.getContent()));
        }
      }
    }
    return str.toString();
  }

  private String getJSForRefresh(final HttpServletRequest request) {
    StringBuilder str = new StringBuilder();
    // parameters for refresh mecanisme
    String space = request.getParameter("SpaceFrom");
    String component = request.getParameter("ComponentFrom");
    String targetFrame = request.getParameter("TargetFrame");
    String jspPage = request.getParameter("JSPPage");

    str.append("document.refreshform.action = '../..").
        append(URLUtil.getURL(null, space, component)).append(jspPage).append("';");
    str.append("document.refreshform.Space.value = '").append(space).append("';");
    str.append("document.refreshform.Component.value = '").append(component).append("';");
    str.append("document.refreshform.target = '").append(targetFrame).append("';");
    str.append("document.refreshform.submit();");
    return str.toString();
  }

  private String getJSForRefreshClipboard() {
    StringBuilder str = new StringBuilder();
    str.append("if(top.ClipboardWindow!=null){");
    str.append("if (!top.ClipboardWindow.closed) {");
    str.append(
        "top.ClipboardWindow = window.open('../../Rclipboard/jsp/clipboardRefresh.jsp'," +
            "'Clipboard','width=350,height=300,alwaysRaised');");
    str.append("}");
    str.append("}");
    return str.toString();
  }

  private String getJSForShowClipboard() {
    StringBuilder str = new StringBuilder();
    // portage netscape
    str.append(
        "top.ClipboardWindow = window.open('../../Rclipboard/jsp/clipboard.jsp','Clipboard'," +
            "'width=500,height=350,alwaysRaised');");
    str.append("top.ClipboardWindow.focus();");
    return str.toString();
  }
}
