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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.silverpeas.peasCore;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.ejb.RemoveException;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.genericPanel.GenericPanel;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.AdminUserConnections;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameter;
import com.stratelia.webactiv.clipboard.control.ejb.Clipboard;
import com.stratelia.webactiv.clipboard.control.ejb.ClipboardBm;
import com.stratelia.webactiv.clipboard.control.ejb.ClipboardBmHome;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBm;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 This object is used by all the components jsp that have access to the session.
 It is initialized given a login and a password which is authenticated.
 It provides functions to get information about the logged user (which is unique).
 It is also used to update the current environnement of the user (current domain, current component).
 */
public class MainSessionController extends AdminReference implements Clipboard {

  ClipboardBm m_ClipboardBm = null;
  PersonalizationBm m_PersonalizationBm = null;
  Object m_ComponentSOFactory = null;
  private String m_sSessionId = null;
  private String m_sUserId = null;
  private OrganizationController organizationController = null;
  private Date userLoginBegin = null;
  private Date userLastRequest = null;
  private String userLanguage = null;
  private ContentManager contentManager = null;
  Hashtable<String, GenericPanel> m_genericPanels = new Hashtable<String, GenericPanel>();
  Selection m_selection = null;
  private String userSpace = null;
  AlertUser m_alertUser = null;
  private String serverName = null;
  private String serverPort = null;
  String m_CurrentSpaceId = null;
  String m_CurrentComponentId = null;
  /** Maintenance Mode **/
  static boolean appInMaintenance = false;
  static List<String> spacesInMaintenance = new ArrayList<String>();
  // Last results from search engine
  private List<GlobalSilverContent> lastResults = null;
  private boolean allowPasswordChange;

  public boolean isAppInMaintenance() {
    return appInMaintenance;
  }

  public void setAppModeMaintenance(boolean mode) {
    SilverTrace.info("peasCore",
        "MainSessionController.setAppModeMaintenance()",
        "root.MSG_GEN_PARAM_VALUE", "mode=" + new Boolean(mode).toString());
    appInMaintenance = mode;
  }

  public boolean isSpaceInMaintenance(String spaceId) {
    spaceId = checkSpaceId(spaceId);
    boolean inMaintenance = spacesInMaintenance.contains(spaceId);
    SilverTrace.info("peasCore", "MainSessionController.isSpaceInMaintenance()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId + ", maintenance = " + inMaintenance);
    return inMaintenance;
  }

  public void setSpaceModeMaintenance(String spaceId, boolean mode) {
    spaceId = checkSpaceId(spaceId);
    SilverTrace.info("peasCore", "MainSessionController.setSpaceModeMaintenance()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId + " mode=" + mode);
    if (mode) {
      if (!spacesInMaintenance.contains(spaceId)) {
        spacesInMaintenance.add(spaceId);
      }
    } else {
      if (spacesInMaintenance.contains(spaceId)) {
        spacesInMaintenance.remove(spacesInMaintenance.indexOf(spaceId));
      }
    }
  }

  /**
   * @param spaceId a space id with or without "WA" prefix
   * @return given spaceId without "WA" prefix
   */
  private String checkSpaceId(String spaceId) {
    if (spaceId != null && spaceId.startsWith(Admin.SPACE_KEY_PREFIX)) {
      // spaceId starts with "WA", return it without prefix
      return spaceId.substring(Admin.SPACE_KEY_PREFIX.length(), spaceId.length());
    }
    return spaceId;
  }

  /** Creates new MainSessionController */
  /** Return an exception if the user is not authenticate */
  /** parameter sKey replaced by sUserId */
  public MainSessionController(String sKey, String sSessionId) throws Exception {
    SilverTrace.info("peasCore", "MainSessionController.constructor()",
        "root.MSG_GEN_PARAM_VALUE", "sKey = " + sKey + " sSessionId="
        + sSessionId);
    try {
      // Authenticate the user
      m_sUserId = m_Admin.authenticate(sKey, sSessionId, isAppInMaintenance());
      m_sSessionId = sSessionId;

      // Get the user language
      userLanguage = getPersonalization().getFavoriteLanguage();
    } catch (Exception e) {
      throw new PeasCoreException(
          "MainSessionController.MainSessionController",
          SilverpeasException.ERROR, "peasCore.EX_CANT_GET_USER_PROFILE",
          "sKey=" + sKey, e);
    }
  }

  public String getUserId() {
    return m_sUserId;
  }

  public String getSessionId() {
    return m_sSessionId;
  }

  /** Return the SilverObject Factory */
  public Object getComponentSOFactory() {
    return m_ComponentSOFactory;
  }

  /** Get the Factory */
  public void setComponentSOFactory(Object Factory) {
    m_ComponentSOFactory = Factory;
  }

  // ------------------- Connexion Functions -----------------------------
  public synchronized void setUserLoginBegin(Date d) {
    userLoginBegin = d;
  }

  public synchronized Date getUserLoginBegin() {
    return userLoginBegin;
  }

  public synchronized void setUserLastRequest(Date d) {
    userLastRequest = d;
  }

  public synchronized Date getUserLastRequest() {
    return userLastRequest;
  }

  // ------------------- Generic Panel Functions -----------------------------
  public void setGenericPanel(String panelKey, GenericPanel panel) {
    m_genericPanels.put(panelKey, panel);
  }

  public GenericPanel getGenericPanel(String panelKey) {
    return (GenericPanel) m_genericPanels.get(panelKey);
  }

  // ------------------- Selection Functions -----------------------------
  public Selection getSelection() {
    if (m_selection == null) {
      m_selection = new Selection();
    }
    return m_selection;
  }

  // ------------------- AlertUser Functions -----------------------------
  public AlertUser getAlertUser() {
    if (m_alertUser == null) {
      m_alertUser = new AlertUser();
    }
    return m_alertUser;
  }

  // ------------------- Clipboard Functions -----------------------------
  public synchronized void initClipboard() {
    m_ClipboardBm = null;
  }

  /** Return the clipboard EJB */
  public synchronized ClipboardBm getClipboard() {
    if (m_ClipboardBm == null) {
      SilverTrace.info("peasCore", "MainSessionController.getClipboard()",
          "root.MSG_GEN_ENTER_METHOD");
      try {
        m_ClipboardBm = ((ClipboardBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.CLIPBOARD_EJBHOME, ClipboardBmHome.class)).create(
            "MainClipboard");
      } catch (Exception e) {
        throw new PeasCoreRuntimeException(
            "MainSessionController.getClipboard()", SilverpeasException.ERROR,
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    SilverTrace.info("peasCore", "MainSessionController.getClipboard()",
        "root.MSG_GEN_EXIT_METHOD");
    return m_ClipboardBm;
  }

  // ------------------- Personalization Functions -----------------------------
  public synchronized void initPersonalization() {
    m_PersonalizationBm = null;
  }

  /** Return the personalization EJB */
  public synchronized PersonalizationBm getPersonalization() {
    if (m_PersonalizationBm == null) {
      // SilverTrace.info("peasCore",
      // "MainSessionController.getPersonalization()",
      // "root.MSG_GEN_ENTER_METHOD");
      try {
        PersonalizationBmHome personalizationBmHome = (PersonalizationBmHome) EJBUtilitaire.
            getEJBObjectRef(JNDINames.PERSONALIZATIONBM_EJBHOME,
            PersonalizationBmHome.class);
        m_PersonalizationBm = personalizationBmHome.create();
        m_PersonalizationBm.setActor(getUserId());
      } catch (Exception e) {
        SilverTrace.error("peasCore",
            "MainSessionController.getPersonalization()",
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
        throw new PeasCoreRuntimeException(
            "MainSessionController.getPersonalization()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    // SilverTrace.info("peasCore",
    // "MainSessionController.getPersonalization()",
    // "root.MSG_GEN_EXIT_METHOD");
    return m_PersonalizationBm;
  }

  /**
   * Return the user's favorite language
   */
  public synchronized String getFavoriteLanguage() {
    if (userLanguage == null) {
      try {
        userLanguage = getPersonalization().getFavoriteLanguage();
      } catch (NoSuchObjectException nsoe) {
        initPersonalization();
        SilverTrace.warn("peasCore",
            "MainSessionController.getFavoriteLanguage()",
            "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
        userLanguage = getFavoriteLanguage();
      } catch (RemoteException e) {
        SilverTrace.error("peasCore",
            "MainSessionController.getFavoriteLanguage()",
            "peasCore.EX_CANT_GET_FAVORITE_LANGUAGE", e);
        userLanguage = "fr";
      }
    }
    return userLanguage;
  }

  /**
   * Return the user's favorite language
   */
  public void setFavoriteLanguage(String newLanguage) {
    userLanguage = newLanguage;
  }

  /**
   * Return the user's favorite space
   */
  public synchronized String getFavoriteSpace() {
    if (userSpace == null) {
      try {
        userSpace = getPersonalization().getPersonalWorkSpace();
        boolean allowed = false;
        String[] availableSpaces = getUserAvailSpaceIds();

        if (userSpace != null) {
          if (!userSpace.equals("null")) {
            // check if this space always exist and if the user have got the
            // rights to access to it
            for (int i = 0; i < availableSpaces.length; i++) {
              if (userSpace.equals(availableSpaces[i])) {
                // the user is allowed to access to this space
                allowed = true;
                break;
              }
            }
          }
        }
        if (!allowed) {
          String[] allRootSpaces = m_Admin.getAllRootSpaceIds();
          userSpace = null;
          // the user is not allowed to access to this space or this space no
          // longer exists or the default space is set to null
          // we must change this space in Personalization
          for (int i = 0; i < availableSpaces.length && userSpace == null; i++) {
            for (int j = 0; j < allRootSpaces.length && userSpace == null; j++) {
              if (allRootSpaces[j].equals(availableSpaces[i])) {
                // the user is allowed to access to this space
                userSpace = allRootSpaces[j];
              }
            }
          }
          getPersonalization().setPersonalWorkSpace(userSpace);
        }
      } catch (NoSuchObjectException nsoe) {
        initPersonalization();
        SilverTrace.warn("peasCore",
            "MainSessionController.getFavoriteSpace()",
            "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
        userSpace = getFavoriteSpace();
      } catch (Exception e) {
        SilverTrace.error("peasCore",
            "MainSessionController.getFavoriteSpace()",
            "peasCore.EX_CANT_GET_FAVORITE_SPACE", e);
        userSpace = null;
      }
    }
    return userSpace;
  }

  public void setFavoriteSpace(String newSpace) {
    userSpace = newSpace;
  }

  /**
   * Return the user's favorite language
   */
  public synchronized String getFavoriteLook() {
    try {
      return getPersonalization().getFavoriteLook();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("peasCore", "MainSessionController.getFavoriteLook()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getFavoriteLook();
    } catch (RemoteException e) {
      SilverTrace.error("peasCore", "MainSessionController.getFavoriteLook()",
          "peasCore.EX_CANT_GET_FAVORITE_LOOK", e);
      return null;
    }
  }

  public synchronized boolean isOnlineEditingEnabled() {
    try {
      return getPersonalization().getOnlineEditingStatus();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return isOnlineEditingEnabled();
    } catch (RemoteException e) {
      SilverTrace.error("peasCore", "MainSessionController.isOnlineEditingEnabled()",
          "peasCore.EX_CANT_GET_ONLINE_EDITING_STATUS", e);
      return false;
    }
  }

  public synchronized boolean isWebDAVEditingEnabled() {
    try {
      return getPersonalization().getWebdavEditingStatus();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return isWebDAVEditingEnabled();
    } catch (RemoteException e) {
      SilverTrace.error("peasCore", "MainSessionController.isWebDAVEditingEnabled()",
          "peasCore.EX_CANT_GET_WEBDAV_EDITING_STATUS", e);
      return false;
    }
  }

  public synchronized boolean isDragNDropEnabled() {
    try {
      return getPersonalization().getDragAndDropStatus();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return isDragNDropEnabled();
    } catch (RemoteException e) {
      SilverTrace.error("peasCore", "MainSessionController.isDragNDropEnabled()",
          "peasCore.EX_CANT_GET_DRAGNDROP_STATUS", e);
      return false;
    }
  }

  // ------------------- Other functions -----------------------------
  public OrganizationController getOrganizationController() {
    if (organizationController == null) {
      organizationController = new OrganizationController();
    }
    return organizationController;
  }

  /** Return the user accesslevel of the cuurent user */
  public String getUserAccessLevel() {
    return getCurrentUserDetail().getAccessLevel();
  }

  /** Return the UserDetail of the the current user */
  public UserDetail getCurrentUserDetail() {
    try {
      return m_Admin.getUserDetail(this.getUserId());
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.getCurrentUserDetail",
          "peasCore.EX_CANT_GET_USER_DETAIL", "userId=" + getUserId(), e);
      return null;
    }
  }

  /** Return the parameters for the given component */
  public List<SPParameter> getComponentParameters(String sComponentId) {
    return m_Admin.getComponentParameters(sComponentId);
  }

  /**
   * Return the value of the parameter for the given component and the given name of parameter
   */
  public String getComponentParameterValue(String sComponentId,
      String parameterName) {
    return m_Admin.getComponentParameterValue(sComponentId, parameterName);
  }

  /** Return the root spaces ids available for the current user */
  public String[] getUserAvailRootSpaceIds() {
    return getOrganizationController().getAllRootSpaceIds(this.getUserId());
  }

  /** Return the components ids available for the current user */
  public String[] getUserAvailComponentIds() {
    SilverTrace.info("peasCore",
        "MainSessionController.getUserAvailComponentIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return m_Admin.getAvailCompoIds(getUserId());
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.getUserAvailComponentIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /** Return the spaces ids available for the current user */
  public String[] getUserAvailSpaceIds() {
    SilverTrace.info("peasCore", "MainSessionController.getUserAvailSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return m_Admin.getClientSpaceIds(m_Admin.getUserSpaceIds(m_sUserId));
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.getUserAvailSpaceIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /** Return the spaces ids manageable by the current user */
  public String[] getUserManageableSpaceIds() {
    SilverTrace.info("peasCore",
        "MainSessionController.getUserManageableSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      UserDetail user = m_Admin.getUserDetail(m_sUserId);
      if (user.getAccessLevel().equals("A") || m_sUserId.equals("0")) {
        return m_Admin.getClientSpaceIds(m_Admin.getAllSpaceIds());
      } else {
        return m_Admin.getClientSpaceIds(m_Admin.getUserManageableSpaceIds(
            m_sUserId));
      }
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.getUserManageableSpaceIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public boolean isBackOfficeVisible() {
    return (getCurrentUserDetail().isBackOfficeVisible()
        || getUserManageableSpaceIds().length > 0 || getUserManageableGroupIds().
        size() > 0);
  }

  public List<String> getUserManageableGroupIds() {
    SilverTrace.info("peasCore",
        "MainSessionController.getUserManageableGroupIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return m_Admin.getUserManageableGroupIds(m_sUserId);
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.getUserManageableGroupIds",
          "admin.MSG_ERR_GET_GROUP", e);
      return null;
    }
  }

  /**
   * Helper function. Create a new CurrentSessionControl object and fill it with the values of the
   * current space Id and component Id passed in parameters
   **/
  public ComponentContext createComponentContext(String sSpaceId,
      String sComponent) {
    ComponentContext newInfos = new ComponentContext();

    try {
      // Set the space
      if (sSpaceId != null) {
        SpaceInstLight spaceInst = m_Admin.getSpaceInstLightById(sSpaceId);

        newInfos.setCurrentSpaceId(sSpaceId);
        newInfos.setCurrentSpaceName(spaceInst.getName(getFavoriteLanguage()));
      }
      // Set the current component and profiles
      if (sComponent != null) {
        String sCurCompoLabel = "";
        ComponentInst componentInst = m_Admin.getComponentInst(sComponent);

        // if (componentInst.getLabel() != null &&
        // componentInst.getLabel().length() != 0)
        sCurCompoLabel = componentInst.getLabel(getFavoriteLanguage());
        newInfos.setCurrentComponentId(sComponent);
        newInfos.setCurrentComponentName(componentInst.getName());
        newInfos.setCurrentComponentLabel(sCurCompoLabel);
        newInfos.setCurrentProfile(m_Admin.getCurrentProfiles(this.getUserId(),
            componentInst));
      }
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.createComponentContext",
          "peasCore.EX_CANT_CREATE_COMPONENT_CONTEXT", "sSpaceId=" + sSpaceId
          + " | sComponent=" + sComponent, e);
    }
    return newInfos;
  }

  /**
   * Update the current space for the current user
   * @deprecated
   */
  public void updateUserSpace(String sSpaceId) {
    m_CurrentSpaceId = sSpaceId;
  }

  /**
   * Update the current component for the current user
   * @deprecated
   */
  public void updateUserComponent(String sComponent) {
    m_CurrentComponentId = sComponent;
  }

  // ---------------------------------
  // Profile functions
  // ---------------------------------
  /**
   * @deprecated
   */
  public String getUserCurrentSpaceId() {
    return m_CurrentSpaceId;
  }

  /**
   * @deprecated
   */
  public String getUserCurrentComponentId() {
    return m_CurrentComponentId;
  }

  /**
   * Get the userId
   */
  public AdminUserConnections getAdminUserConnections() {
    AdminUserConnections auc = new AdminUserConnections(m_sUserId, m_sSessionId);
    return auc;
  }

  // ------------------- ContentManager Functions -----------------------------
  public ContentManager getContentManager() {
    try {
      if (contentManager == null) {
        contentManager = new ContentManager();
      }
    } catch (Exception e) {
      SilverTrace.error("peasCore", "MainSessionController.getContentManager",
          "peasCore.EX_UNABLE_TO_GET_CONTENTMANAGER", e);
    }
    return contentManager;
  }

  public void initServerProps(String sName, String sPort) {
    serverName = sName;
    serverPort = sPort;
  }

  public String getServerName() {
    return serverName;
  }

  public String getServerPort() {
    return serverPort;
  }

  public String getServerNameAndPort() {
    if (!StringUtil.isDefined(serverPort)) {
      return serverName;
    } else {
      return serverName + ":" + serverPort;
    }
  }

  public void close() {
    try {
      if (m_PersonalizationBm != null) {
        m_PersonalizationBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("peasCore", "MainSessionController.close",
          "peasCore.EX_UNABLE_TO_REMOVE_EJB", e);
    } catch (RemoveException e) {
      SilverTrace.error("peasCore", "MainSessionController.close",
          "peasCore.EX_UNABLE_TO_REMOVE_EJB", e);
    }

    try {
      if (m_ClipboardBm != null) {
        m_ClipboardBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("peasCore", "MainSessionController.close",
          "peasCore.EX_UNABLE_TO_REMOVE_EJB", e);
    } catch (RemoveException e) {
      SilverTrace.error("peasCore", "MainSessionController.close",
          "peasCore.EX_UNABLE_TO_REMOVE_EJB", e);
    }
  }

  /**
   * @return a List of GlobalSilverResult corresponding to the last search
   */
  public List<GlobalSilverContent> getLastResults() {
    return lastResults;
  }

  /**
   * @param list
   */
  public void setLastResults(List<GlobalSilverContent> list) {
    lastResults = list;
  }

  public void setAllowPasswordChange(boolean flag) {
    this.allowPasswordChange = flag;

  }

  public boolean isAllowPasswordChange() {
    return allowPasswordChange;
  }

  @Override
  public void add(ClipboardSelection clipObject) throws
      RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.add(clipObject);
    }
  }

  @Override
  public ClipboardSelection getObject() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getObject();
    }
  }

  @Override
  public void PasteDone() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.PasteDone();
    }
  }

  @Override
  public Collection<?> getSelectedObjects() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getSelectedObjects();
    }
  }

  @Override
  public Collection<?> getObjects() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getObjects();
    }
  }

  @Override
  public int size() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.size();
    }
  }

  @Override
  public ClipboardSelection getObject(int index) throws
      RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getObject(index);
    }
  }

  @Override
  public void setSelected(int index, boolean setIt) throws
      RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.setSelected(index, setIt);
    }
  }

  @Override
  public void removeObject(int index) throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.removeObject(index);
    }
  }

  @Override
  public void clear() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.clear();
    }
  }

  @Override
  public void setMultiClipboard() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.setMultiClipboard();
    }
  }

  @Override
  public void setSingleClipboard() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.setSingleClipboard();
    }
  }

  @Override
  public String getName() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getName();
    }
  }

  @Override
  public Integer getCount() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getCount();
    }
  }

  @Override
  public String getMessageError() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getMessageError();
    }
  }

  @Override
  public Exception getExceptionError() throws RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getExceptionError();
    }
  }

  @Override
  public void setMessageError(String messageID, Exception e) throws
      RemoteException {
    ClipboardBm clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.setMessageError(messageID, e);
    }
  }
}
