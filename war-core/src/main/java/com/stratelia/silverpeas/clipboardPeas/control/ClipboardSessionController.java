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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.clipboardPeas.control;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

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
import com.stratelia.webactiv.clipboard.control.ejb.ClipboardBm;

import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

// import com.stratelia.webactiv.util.publication.model.*;

/**
 * A servlet ClipboardSessionControler acts as a proxy for a ClipboardBm EJB.
 */
public class ClipboardSessionController extends
    AbstractComponentSessionController {

  /*
   * The Web'Activ context
   */
  private int m_counter = 0;

  /*
   * Attributes from the caller component (paste operation)
   */
  private String m_CallerRooterName;
  private String m_CallerComponentId;
  private String m_CallerSpaceId;
  private String m_CallerJSPPage;
  private String m_CallerTargetFrame;

  /**
   * The bundle containing all the settings.
   */
  private ResourceLocator settings = null;

  private String sessionId = null;

  /**
   * The ClipboardSessionController is built empty and will be later
   * initialized.
   */
  public ClipboardSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.stratelia.webactiv.clipboard.multilang.clipboard",
        "com.stratelia.webactiv.clipboard.settings.clipboardIcons");
    sessionId = mainSessionCtrl.getSessionId();
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Method getCounter
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getCounter() {
    return m_counter;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Method incCounter
   * 
   * 
   * @return
   * 
   * @see
   */
  public void incCounter(int inc) {
    m_counter += inc;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Method doIdle
   * 
   * 
   * @return
   * 
   * @see
   */
  public void doIdle(int nbinc) {
    incCounter(nbinc);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Method getJavaScriptTask
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getHF_JavaScriptTask(HttpServletRequest request) {
    String message = request.getParameter("message");
    StringBuffer str = new StringBuffer("");

    if (message != null) {
      SilverTrace.info("clipboardPeas",
          "ClipboardSessionController.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", " message = " + message);

      if (message.equals("SHOWCLIPBOARD")) {
        // portage netscape
        str
            .append("top.ClipboardWindow = window.open('../../Rclipboard/jsp/clipboard.jsp','Clipboard','width=500,height=350,alwaysRaised');");
        str.append("top.ClipboardWindow.focus();");
      } else if (message.equals("REFRESHCLIPBOARD")) {
        // portage netscape
        str.append("if(top.ClipboardWindow!=null){");
        str.append("if (!top.ClipboardWindow.closed) {");
        str
            .append("top.ClipboardWindow = window.open('../../Rclipboard/jsp/clipboardRefresh.jsp','Clipboard','width=350,height=300,alwaysRaised');");
        str.append("}");
        str.append("}");

      } else if (message.equals("REFRESH")) {
        // parameters for refresh mecanisme
        String Space = request.getParameter("SpaceFrom");
        String Component = request.getParameter("ComponentFrom");
        String TargetFrame = request.getParameter("TargetFrame");
        String JSPPage = request.getParameter("JSPPage");

        str.append("document.refreshform.action = '../.."
            + URLManager.getURL(null, Space, Component) + JSPPage + "';");
        str.append("document.refreshform.Space.value = '" + Space + "';");
        str.append("document.refreshform.Component.value = '" + Component
            + "';");
        str.append("document.refreshform.target = '" + TargetFrame + "';");
        str.append("document.refreshform.submit();");
      } else if (message.equals("IDLE")) {
        com.stratelia.silverpeas.notificationserver.channel.server.SilverMessage serverMessage = com.stratelia.silverpeas.notificationserver.channel.server.SilverMessageFactory
            .read(getUserId(), sessionId);

        if (serverMessage != null) {
          SilverTrace.info("clipboardPeas",
              "ClipboardSessionController.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", " serverMessage = " + serverMessage);
          if (serverMessage.getWhat().equals("ALERT")) {
            str.append("alert ('");
            str.append(EncodeHelper.javaStringToJsString(serverMessage
                .getContent()));
            str.append("');");
            str
                .append("self.location.href = '../../Rclipboard/jsp/Idle.jsp?message=DELMSG&messageTYPE=SERVER&messageID="
                    + serverMessage.getID() + "';");
          } else if (serverMessage.getWhat().equals("JAVASCRIPT")) {
            str.append(EncodeHelper.javaStringToJsString(serverMessage
                .getContent()));
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
              str.append("msgPopup = SP_openWindow('../.."
                  + URLManager.getURL(URLManager.CMP_POPUP)
                  + "ReadMessage.jsp?MessageID=" + popupMessage.getID()
                  + "','popupmsg" + new Long(new Date().getTime()).toString()
                  + "',500,260,'scrollbars=yes');");
            } else if (popupMessage.getWhat().equals("JAVASCRIPT")) {
              str.append(EncodeHelper.javaStringToJsString(popupMessage
                  .getContent()));
            }
            // CBO : ADD
            else if (popupMessage.getWhat().equals("COMMUNICATION")) {
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
              str.append("OpenDiscussion('../.."
                  + URLManager.getURL(URLManager.CMP_COMMUNICATIONUSER)
                  + "OpenDiscussion?userId=" + popupMessage.getSenderId()
                  + "&MessageID=" + popupMessage.getID() + "','popupDiscussion"
                  + popupMessage.getSenderId()
                  + "',650,400,'menubar=no,scrollbars=no,statusbar=no');");
            }
            // CBO : FIN ADD
          }
        }
      } else if (message.equals("DELMSG")) {
        String ID = request.getParameter("messageID");
        String TYPE = request.getParameter("messageTYPE");

        if (TYPE != null) {
          if (TYPE.equals("SERVER")) {
            com.stratelia.silverpeas.notificationserver.channel.server.SilverMessageFactory
                .del(ID);
          } else if (TYPE.equals("POPUP")) {
            com.stratelia.silverpeas.notificationserver.channel.popup.SilverMessageFactory
                .del(ID);
          }
        }
      }
    }
    return str.toString();
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Method getJavaScriptTask
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getHF_HTMLForm(HttpServletRequest request) {
    String message = request.getParameter("message");
    StringBuffer str = new StringBuffer("");

    if (message != null) {
      if (message.equals("ALERT")) {
      } else if (message.equals("REFRESH")) {
        str
            .append("<form name='refreshform' action='' method='post' target='MyMain'>");
        str.append("<input type='hidden' name='Space'>");
        str.append("<input type='hidden' name='Component'>");
        str.append("</form>");
      }
    }
    return str.toString();
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Method getMessageError
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getMessageError() {
    String message = null;
    Exception exc = null;
    ClipboardBm clipboard = getClipboard();

    try {
      message = clipboard.getMessageError();
      if (message != null) {
        message = getString(message);
        exc = clipboard.getExceptionError();
        if (exc != null) {
          message = message + exc.getMessage();
        }
      }
    } catch (Exception e) {
      SilverTrace.info("clipboardPeas",
          "ClipboardSessionController.getMessageError()",
          "clipboardPeas.EX_CANT_GET_MESSAGE", "", e);
    }
    return message;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Return the list of object (IndexEntry
   * format) in clipbord.
   * 
   * @return
   * 
   * @see
   */
  public Collection getIndexEntryObjects() throws java.rmi.RemoteException {
    SilverTrace.info("clipboardPeas",
        "ClipboardSessionController.getStrateliaReferenceObjects()",
        "root.MSG_GEN_ENTER_METHOD");
    ClipboardBm clipboard = getClipboard();
    ArrayList result = new ArrayList();
    Collection clipObjects = clipboard.getObjects();
    Iterator qi = clipObjects.iterator();

    while (qi.hasNext()) {
      Transferable clipObject = (Transferable) qi.next();

      if ((clipObject != null)
          && (clipObject.isDataFlavorSupported(ClipboardSelection.IndexFlavor))) {
        // ce qu'il faut faire
        // (clipObject.isDataFlavorSupported (StrateliaSelection.urlFlavor))) {
        try {
          IndexEntry indexEntry;

          indexEntry = (IndexEntry) clipObject
              .getTransferData(ClipboardSelection.IndexFlavor);
          result.add(indexEntry);
        } catch (Exception e) {
          SilverTrace.error("clipboardPeas",
              "ClipboardSessionController.getIndexEntryObjects()",
              "root.EX_CLIPBOARD_PASTE_FAILED", "", e);
        }
        // result.add (clipObject);
      }
    }
    SilverTrace.info("clipboardPeas",
        "ClipboardSessionController.getStrateliaReferenceObjects()",
        "root.MSG_GEN_EXIT_METHOD");
    return result;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Return the list of object in clipbord.
   * 
   * @return
   * 
   * @see
   */
  public Collection getObjects() throws java.rmi.RemoteException,
      javax.naming.NamingException, java.sql.SQLException {
    ClipboardBm clipboard = getClipboard();
    ArrayList result = new ArrayList();
    Collection clipObjects = clipboard.getObjects();
    Iterator qi = clipObjects.iterator();

    while (qi.hasNext()) {
      ClipboardSelection clipObject = (ClipboardSelection) qi.next();

      result.add(clipObject);
    }
    return result;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public ResourceLocator getSettings() {
    if (settings == null) {
      settings = new ResourceLocator(
          "com.stratelia.webactiv.clipboard.settings.clipboardSettings", "");
    }
    return settings;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Returns the label of the given domain/space
   */
  public String getSpaceLabel(String spaceId) {
    SpaceInst spaceInst = getOrganizationController().getSpaceInstById(spaceId);

    if (spaceInst != null) {
      return spaceInst.getName();
    } else {
      return spaceId;
    }
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Returns the label of the given component
   */
  public String getComponentLabel(String componentId) {
    ComponentInst componentInst = getOrganizationController().getComponentInst(
        componentId);

    if (componentInst != null) {
      if (componentInst.getLabel().length() > 0) {
        return componentInst.getLabel();
      } else {
        return componentInst.getName();
      }
    } else {
      SilverTrace.error("clipboardPeas",
          "ClipboardSessionController.getComponentLabel()",
          "clipboardPeas.EX_CANT_GET_COMPO_LABEL");

      return componentId;
    }
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public void setComponentRooterName(String RooterName) {
    m_CallerRooterName = RooterName;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public void setSpaceId(String SpaceId) {
    m_CallerSpaceId = SpaceId;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public void setComponentId(String ComponentId) {
    m_CallerComponentId = ComponentId;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public void setJSPPage(String JSPPage) {
    m_CallerJSPPage = JSPPage;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public void setTargetFrame(String TargetFrame) {
    m_CallerTargetFrame = TargetFrame;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public String getComponentRooterName() {
    return m_CallerRooterName;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public String getSpaceId() {
    return m_CallerSpaceId;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public String getComponentId() {
    return m_CallerComponentId;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public String getJSPPage() {
    return m_CallerJSPPage;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public String getTargetFrame() {
    return m_CallerTargetFrame;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getIntervalInSec() {
    return getSettings().getString("IntervalInSec");
  }

}
