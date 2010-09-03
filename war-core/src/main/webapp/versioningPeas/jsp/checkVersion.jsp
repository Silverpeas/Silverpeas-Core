<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page isELIgnored="false"%>
<%@ page import="java.util.ArrayList,
				 java.util.List,
         java.util.Hashtable,
         java.util.Iterator,
         java.util.Date,
         java.util.Collection,
         java.util.StringTokenizer,
         java.rmi.RemoteException,
         java.net.URLEncoder,
         java.beans.*,
         java.io.File,
         java.io.PrintWriter,
         java.io.IOException,
         java.io.FileInputStream,
         java.io.ObjectInputStream,
         java.sql.SQLException,
         javax.ejb.RemoveException,
         javax.ejb.CreateException,
         javax.ejb.FinderException,
         javax.servlet.*,
         javax.servlet.http.*,
         javax.servlet.jsp.*,
         javax.naming.NamingException,
         com.stratelia.webactiv.util.viewGenerator.html.Encode,
         com.stratelia.webactiv.util.WAPrimaryKey,
         com.stratelia.silverpeas.peasCore.URLManager,
         com.stratelia.silverpeas.silvertrace.SilverTrace,
         com.stratelia.silverpeas.versioningPeas.control.VersioningSessionController,
         com.stratelia.webactiv.util.fileFolder.FileFolderManager,
         com.stratelia.webactiv.util.FileRepositoryManager,
         com.stratelia.webactiv.util.GeneralPropertiesManager,
         com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory,
         com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine,
         com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn,
         com.stratelia.webactiv.util.viewGenerator.html.frame.Frame,
         com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList,
         com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*,
         com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar,
         com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane,
         com.stratelia.webactiv.util.viewGenerator.html.icons.Icon,
         com.stratelia.webactiv.util.viewGenerator.html.window.Window,
         com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane,
         com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane,
         com.stratelia.webactiv.util.viewGenerator.html.buttons.Button,
         com.stratelia.webactiv.util.viewGenerator.html.board.Board,
         com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane,
         com.stratelia.webactiv.util.viewGenerator.html.window.Window,
         com.stratelia.silverpeas.versioning.model.Document,
         com.stratelia.silverpeas.versioning.model.DocumentPK,
         com.stratelia.silverpeas.versioning.model.DocumentVersion,
         com.stratelia.silverpeas.versioning.model.DocumentVersionPK,
         com.stratelia.silverpeas.versioning.model.Worker,
         com.stratelia.silverpeas.versioning.model.Reader,
         com.stratelia.silverpeas.versioning.util.VersioningUtil,
         com.stratelia.silverpeas.peasCore.MainSessionController,
         com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.silverpeas.util.ForeignPK"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.silverpeas.peasCore.ComponentContext"%>

<%!
  VersioningSessionController setComponentSessionController(HttpSession session, MainSessionController mainSessionCtrl, String componentId) {
    //ask to MainSessionController to create the ComponentContext
    ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, componentId);
    //instanciate a new CSC
    VersioningSessionController component = new VersioningSessionController(mainSessionCtrl, componentContext);
    session.setAttribute("Silverpeas_versioningPeas", component);
    return component;
  }

  boolean isWriter(List users, int user_id) {
    boolean is_writer = false;
    for (int i = 0; i < users.size(); i++) {
      Worker user = (Worker) users.get(i);
      if (user.getUserId() == user_id) {
        if (user.isWriter()) {
          is_writer = true;
        }

        break;
      }
    }

    return is_writer;
  }

  boolean isValidator(List users, int user_id) {
    boolean is_validator = false;
    for (int i = 0; i < users.size(); i++) {
      Worker user = (Worker) users.get(i);
      if (user.getUserId() == user_id) {
        if (user.isApproval()) {
          is_validator = true;
        }

        break;
      }
    }

    return is_validator;
  }

  boolean isExist(List users, int user_id) {
    boolean is_exist = false;
    for (int i = 0; i < users.size(); i++) {
      Worker user = (Worker) users.get(i);
      if (user.getUserId() == user_id) {
        is_exist = true;

        break;
      }
    }

    return is_exist;
  }

  List removeReiteration(List users) {
    Hashtable user_ids = new Hashtable(users.size());
    int i = 0;
    while (i < users.size()) {
      Worker user = (Worker) users.get(i);
      if (user_ids.containsKey(String.valueOf(user.getUserId()))) {
        users.remove(i);
      } else {
        user_ids.put(String.valueOf(user.getUserId()), "");
        i++;
      }
    }

    return users;
  }

  public boolean isUserReader(Document document, int user_id, VersioningSessionController versioning_sc) throws RemoteException {
    try {
      List readers = document.getReadList();
      List writers = versioning_sc.getAllNoReader(document);
      com.stratelia.silverpeas.versioning.model.Reader user;

      for (int i = 0; i < readers.size(); i++) {
        user = (com.stratelia.silverpeas.versioning.model.Reader) readers.get(i);
        if (user.getUserId() == user_id) {
          return true;
        }
      }

      for (int i = 0; i < writers.size(); i++) {
        user = (com.stratelia.silverpeas.versioning.model.Reader) writers.get(i);
        if (user.getUserId() == user_id) {
          return true;
        }
      }
    } catch (Exception e) {
      SilverTrace.error("versioning", "checkVersionjsp", "root.EX_REMOTE_EXCEPTION", e);
    }

    return false;
  }

  public boolean isUserWriter(Document document, int user_id) {
    List writers = document.getWorkList();
    for (int i = 0; i < writers.size(); i++) {
      Worker user = (Worker) writers.get(i);
      if (user.getUserId() == user_id) {
        return true;
      }
    }
    return false;
  }

  private boolean isFileSharingEnable(MainSessionController msc, String componentId) {
    String param = msc.getOrganizationController().getComponentParameterValue(componentId, "useFileSharing");
    return "yes".equalsIgnoreCase(param);
  }
%>

<%
      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
      VersioningSessionController versioningSC = (VersioningSessionController) request.getAttribute(URLManager.CMP_VERSIONINGPEAS);
      ResourcesWrapper resources = (ResourcesWrapper) request.getAttribute("resources");
      String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
      ResourceLocator attachmentSettings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
      MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
      boolean webdavEditingEnable = m_MainSessionCtrl.isWebDAVEditingEnabled() && attachmentSettings.getBoolean("OnlineEditingEnable", false);
      boolean dragAndDropEnable = m_MainSessionCtrl.isDragNDropEnabled() && attachmentSettings.getBoolean("DragAndDropEnable", false);
      String sURI = request.getRequestURI();
      String sRequestURL = request.getRequestURL().toString();
      String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
      String httpServerBase = GeneralPropertiesManager.getGeneralResourceLocator().getString("httpServerBase", m_sAbsolute);
      Window window = gef.getWindow();
      Frame frame = gef.getFrame();
      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setClickable(false);
      OperationPane operationPane = window.getOperationPane();

      String userPanelIcon = m_context + "/util/icons/readingControl.gif";
      String hLineSrc = m_context + "/util/icons/colorPix/1px.gif";
      String groupSrc = m_context + "/util/icons/groupe.gif";
      String userSrc = m_context + "/util/icons/user.gif";
      String scheduledGroupSrc = m_context + "/jobDomainPeas/jsp/icons/scheduledGroup.gif";
      String saveListIcon = m_context + "/util/icons/saveAccessList.gif";
      String userPanelDeleteIcon = m_context + "/util/icons/userPanelPeas_to_del.gif";

      ResourceLocator attMessages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
      ResourcesWrapper attResources = new ResourcesWrapper(attMessages, null, null, m_MainSessionCtrl.getFavoriteLanguage());
%>