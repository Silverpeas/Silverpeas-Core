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
  private final String sessionId;

  public ClipboardSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.clipboard.multilang.clipboard",
        "org.silverpeas.clipboard.settings.clipboardIcons");
    sessionId = mainSessionCtrl.getSessionId();
  }

  public int getCounter() {
    return counter;
  }

  public void incCounter(int inc) {
    counter += inc;
  }

  public void doIdle(int inc) {
    incCounter(inc);
  }

  public String getJavaScriptTaskForHiddenFrame(HttpServletRequest request) {
    String message = Encode.forHtml(request.getParameter("message"));
    String js;
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
      js = "";
    } else {
      js = "";
    }
    return js;
  }

  public String getHTMLFormForHiddenFrame(HttpServletRequest request) {
    String message = Encode.forHtml(request.getParameter("message"));
    StringBuilder str = new StringBuilder();
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
   * Return the list of object in clipboard.
   *
   * @return the list of object in clipboard.
   * @throws ClipboardException if an error occurs
   */
  public synchronized Collection<ClipboardSelection> getObjects() throws ClipboardException {
    return new ArrayList<>(getClipboardObjects());
  }

  @Override
  public SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle("org.silverpeas.clipboard.settings.clipboardSettings");
  }

  /**
   * Returns the label of the given component
   *
   * @param componentId the unique identifier of a component instance.
   * @return the label of the given component instance.
   */
  public String getComponentLabel(String componentId) {
    ComponentInst componentInst = getOrganisationController().getComponentInst(componentId);
    if (componentInst != null) {
      if (!componentInst.getLabel().isEmpty()) {
        return componentInst.getLabel();
      }
      return componentInst.getName();
    }
    SilverLogger.getLogger(this).error(failureOnGetting("component instance", componentId));
    return componentId;
  }

  public void setComponentRooterName(String rooterName) {
    callerRooterName = rooterName;
  }

  public void setSpaceId(String spaceId) {
    this.context.setCurrentSpaceId(spaceId);
  }

  public void setComponentId(String componentId) {
    this.context.setCurrentComponentId(componentId);
  }

  public void setJSPPage(String jspPage) {
    callerJSPPage = jspPage;
  }

  public void setTargetFrame(String targetFrame) {
    callerTargetFrame = targetFrame;
  }

  public String getComponentRooterName() {
    return callerRooterName;
  }

  public String getJSPPage() {
    return callerJSPPage;
  }

  public String getTargetFrame() {
    return callerTargetFrame;
  }

  public String getIntervalInSec() {
    return getSettings().getString("IntervalInSec");
  }

  private String getJSForIdle() {
    StringBuilder str = new StringBuilder();
    ServerMsg serverMsg = ServerMessageService.get().read(getUserId(), sessionId);

    if (serverMsg != null) {
      if ("ALERT".equals(serverMsg.getWhat())) {
        str.append("alert ('")
            .append(Encode.forJavaScript(serverMsg.getContent()))
            .append("');")
            .append("self.location.href = '../../Rclipboard/jsp/Idle.jsp?")
            .append("message=DELMSG&messageTYPE=SERVER&messageID=")
            .append(serverMsg.getID()).append("';");
      } else if ("JAVASCRIPT".equals(serverMsg.getWhat())) {
        str.append(Encode.forJavaScript(serverMsg.getContent()));
      }
    } else {
      PopupMsg popupMessage = PopupMessageService.get().read(getUserId());
      if (popupMessage != null) {
        if ("ALERT".equals(popupMessage.getWhat())) {
          str.append("msgPopup = SP_openWindow('../..").
              append(URLUtil.getURL(URLUtil.CMP_POPUP, null, null)).
              append("ReadMessage.jsp?MessageID=").append(popupMessage.getID()).
              append("','popupmsg").
              append(new Date().getTime()).append("',500,260,'scrollbars=yes');");
        } else if ("JAVASCRIPT".equals(popupMessage.getWhat())) {
          str.append(Encode.forJavaScript(popupMessage.getContent()));
        }
      }
    }
    return str.toString();
  }

  private String getJSForRefresh(final HttpServletRequest request) {
    StringBuilder str = new StringBuilder();
    // parameters for refresh mechanism
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
    return "if(top.ClipboardWindow!=null){" +
        "if (!top.ClipboardWindow.closed) {" +
        "top.ClipboardWindow = window.open('../../Rclipboard/jsp/clipboardRefresh.jsp'," +
        "'Clipboard','width=350,height=300,alwaysRaised');" +
        "}" +
        "}";
  }

  private String getJSForShowClipboard() {
    // portage netscape
    return "top.ClipboardWindow = window.open('../../Rclipboard/jsp/clipboard.jsp'," +
        "'Clipboard','width=500,height=350,alwaysRaised');top.ClipboardWindow.focus();";
  }
}
