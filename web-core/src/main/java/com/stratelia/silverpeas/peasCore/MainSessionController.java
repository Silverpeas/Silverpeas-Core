/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.peasCore;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.admin.components.Parameter;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardException;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.genericPanel.GenericPanel;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.control.PdcSettings;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminUserConnections;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.clipboard.control.ejb.Clipboard;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.subscription.SubscriptionContext;

import static com.stratelia.webactiv.beans.admin.AdminReference.getAdminService;

/*
 This object is used by all the components jsp that have access to the session.
 It is initialized given a login and a password which is authenticated.
 It provides functions to get information about the logged user (which is unique).
 It is also used to update the current environnement of the user (current domain, current component).
 */
public class MainSessionController implements Clipboard {

  public static final String MAIN_SESSION_CONTROLLER_ATT = "SilverSessionController";
  Clipboard clipboard = null;
  final UserPreferences userPreferences;
  PdcBm pdcBm = null;
  Object m_ComponentSOFactory = null;
  private String sessionId = null;
  private String userId = null;
  private OrganisationController organizationController = null;
  private Date userLoginBegin = null;
  private Date userLastRequest = null;
  private String userLanguage = null;
  private ContentManager contentManager = null;
  Map<String, GenericPanel> genericPanels = Collections.synchronizedMap(
      new HashMap<String, GenericPanel>());
  Selection selection = null;
  private String userSpace = null;
  AlertUser m_alertUser = null;
  private String serverName = null;
  private String serverPort = null;
  String m_CurrentSpaceId = null;
  String m_CurrentComponentId = null;
  private SubscriptionContext subscriptionContext = null;
  /**
   * Maintenance Mode *
   */
  static boolean appInMaintenance = false;
  static List<String> spacesInMaintenance = new ArrayList<String>();
  // Last results from search engine
  private List<GlobalSilverContent> lastResults = null;
  private boolean allowPasswordChange;

  public final boolean isAppInMaintenance() {
    return appInMaintenance;
  }

  public void setAppModeMaintenance(boolean mode) {
    SilverTrace.info("peasCore", "MainSessionController.setAppModeMaintenance()",
        "root.MSG_GEN_PARAM_VALUE", "mode=" + String.valueOf(mode));
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
      return spaceId.substring(Admin.SPACE_KEY_PREFIX.length(), spaceId.length());
    }
    return spaceId;
  }

  /**
   * Default constructor just for tests.
   */
  protected MainSessionController() {
    userPreferences = null;
  }

  /**
   * Creates new MainSessionController
   */
  /**
   * Return an exception if the user is not openSession
   */
  /**
   * parameter authenticationKey replaced by sUserId
   */
  public MainSessionController(String authenticationKey, String sessionId) throws Exception {
    SilverTrace.info("peasCore",
        "MainSessionController.constructor()", "root.MSG_GEN_PARAM_VALUE",
        "authenticationKey = " + authenticationKey + " sessionId=" + sessionId);
    try {
      // Identify the user
      this.userId = getAdminService().identify(authenticationKey, sessionId, isAppInMaintenance());
      this.sessionId = sessionId;
      this.userPreferences = SilverpeasServiceProvider.getPersonalizationService()
          .getUserSettings(userId);

      // Get the user language
      userLanguage = userPreferences.getLanguage();
    } catch (Exception e) {
      throw new PeasCoreException("MainSessionController.MainSessionController",
          SilverpeasException.ERROR, "peasCore.EX_CANT_GET_USER_PROFILE",
          "authenticationKey=" + authenticationKey, e);
    }
  }

  public String getUserId() {
    return userId;
  }

  public String getSessionId() {
    return sessionId;
  }

  /**
   * Return the SilverObject Factory
   */
  public Object getComponentSOFactory() {
    return m_ComponentSOFactory;
  }

  /**
   * Get the Factory
   */
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
    genericPanels.put(panelKey, panel);
  }

  public GenericPanel getGenericPanel(String panelKey) {
    return genericPanels.get(panelKey);
  }

  // ------------------- Selection Functions -----------------------------
  public Selection getSelection() {
    if (selection == null) {
      selection = new Selection();
    }
    return selection;
  }

  // ------------------- Subscription Functions -----------------------------
  public SubscriptionContext getSubscriptionContext() {
    if (subscriptionContext == null) {
      subscriptionContext = new SubscriptionContext(getCurrentUserDetail(), userPreferences);
    }
    return subscriptionContext;
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
    clipboard = null;
  }

  /**
   * Return the clipboard EJB
   */
  public synchronized Clipboard getClipboard() {
    if (clipboard == null) {
      SilverTrace.info("peasCore", "MainSessionController.getClipboard()",
          "root.MSG_GEN_ENTER_METHOD");
      try {
        clipboard = create("MainClipboard");
      } catch (Exception e) {
        throw new PeasCoreRuntimeException("MainSessionController.getClipboard()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    SilverTrace.info("peasCore", "MainSessionController.getClipboard()", "root.MSG_GEN_EXIT_METHOD");
    return clipboard;
  }

  /**
   * Return the personalization EJB
   */
  public UserPreferences getPersonalization() {
    return userPreferences;
  }

  /**
   * Return the user's favorite language
   */
  public String getFavoriteLanguage() {
    if (userLanguage == null) {
      userLanguage = userPreferences.getLanguage();
    }
    return userLanguage;
  }

  /**
   * Return the user's favorite space
   */
  public synchronized String getFavoriteSpace() {
    if (userSpace == null) {
      userSpace = userPreferences.getPersonalWorkSpaceId();
      boolean allowed = false;
      if (StringUtil.isDefined(userSpace)) {
        // check if this space always exist and if the user have the right to access to it
        allowed = getOrganisationController().isSpaceAvailable(userSpace, getUserId());
      }
      if (!allowed) {
        getPersonalization().setPersonalWorkSpaceId("");
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
  public String getFavoriteLook() {
    return userPreferences.getLook();
  }

  /**
   * @return @deprecated use isWebDAVEditingEnabled instead.
   */
  public boolean isOnlineEditingEnabled() {
    return userPreferences.isWebdavEditionEnabled();
  }

  public boolean isWebDAVEditingEnabled() {
    return userPreferences.isWebdavEditionEnabled();
  }

  public boolean isDragNDropEnabled() {
    return userPreferences.isDragAndDropEnabled();
  }

  // ------------------- Other functions -----------------------------
  public OrganisationController getOrganisationController() {
    if (organizationController == null) {
      organizationController = OrganisationControllerFactory.getOrganisationController();
    }
    return organizationController;
  }

  /**
   * Return the user accesslevel of the cuurent user
   */
  public UserAccessLevel getUserAccessLevel() {
    return getCurrentUserDetail().getAccessLevel();
  }

  /**
   * Return the UserDetail of the the current user
   */
  public UserDetail getCurrentUserDetail() {
    try {
      return getAdminService().getUserDetail(this.getUserId());
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.getCurrentUserDetail",
          "peasCore.EX_CANT_GET_USER_DETAIL", "userId=" + getUserId(), e);
      return null;
    }
  }

  /**
   * Return the parameters for the given component
   */
  public List<Parameter> getComponentParameters(String sComponentId) {
    return getAdminService().getComponentParameters(sComponentId);
  }

  /**
   * Return the value of the parameter for the given component and the given name of parameter
   */
  public String getComponentParameterValue(String sComponentId, String parameterName) {
    return getAdminService().getComponentParameterValue(sComponentId, parameterName);
  }

  /**
   * Return the root spaces ids available for the current user
   */
  public String[] getUserAvailRootSpaceIds() {
    return getOrganisationController().getAllRootSpaceIds(this.getUserId());
  }

  /**
   * Return the components ids available for the current user
   */
  public String[] getUserAvailComponentIds() {
    SilverTrace.info("peasCore",
        "MainSessionController.getUserAvailComponentIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAvailCompoIds(getUserId());
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.getUserAvailComponentIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /**
   * Return the spaces ids available for the current user
   */
  public String[] getUserAvailSpaceIds() {
    SilverTrace.info("peasCore", "MainSessionController.getUserAvailSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getClientSpaceIds(getAdminService().getUserSpaceIds(userId));
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "MainSessionController.getUserAvailSpaceIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /**
   * Return the spaces ids manageable by the current user
   */
  public String[] getUserManageableSpaceIds() {
    SilverTrace.info("peasCore",
        "MainSessionController.getUserManageableSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      UserDetail user = getAdminService().getUserDetail(userId);
      if (user.isAccessAdmin() || userId.equals("0")) {
        return getAdminService().getClientSpaceIds(getAdminService().getAllSpaceIds());
      } else {
        return getAdminService().getClientSpaceIds(getAdminService().getUserManageableSpaceIds(
            userId));
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
        || getUserManageableSpaceIds().length > 0 || !getUserManageableGroupIds().isEmpty()
        || isPDCBackOfficeVisible());
  }

  public boolean isPDCBackOfficeVisible() {
    if (!PdcSettings.delegationEnabled) {
      return false;
    }

    try {
      // First, check if user is directly manager of a part of PDC
      return getPdcBm().isUserManager(userId);
    } catch (PdcException e) {
      SilverTrace.error("peasCore", "MainSessionController.isPDCBackOfficeVisible",
          "admin.MSG_ERR_GET_PDC_VISIBILITY", e);
    }

    return false;
  }

  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = new PdcBmImpl();
    }

    return pdcBm;
  }

  public List<String> getUserManageableGroupIds() {
    SilverTrace.info("peasCore",
        "MainSessionController.getUserManageableGroupIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getUserManageableGroupIds(userId);
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
   */
  public ComponentContext createComponentContext(String sSpaceId,
      String sComponent) {
    ComponentContext newInfos = new ComponentContext();

    try {
      // Set the space
      if (sSpaceId != null) {
        SpaceInstLight spaceInst = getAdminService().getSpaceInstLightById(sSpaceId);

        newInfos.setCurrentSpaceId(sSpaceId);
        newInfos.setCurrentSpaceName(spaceInst.getName(getFavoriteLanguage()));
      }
      // Set the current component and profiles
      if (sComponent != null) {
        String sCurCompoLabel = "";
        ComponentInst componentInst = getAdminService().getComponentInst(sComponent);

        // if (componentInst.getLabel() != null &&
        // componentInst.getLabel().length() != 0)
        sCurCompoLabel = componentInst.getLabel(getFavoriteLanguage());
        newInfos.setCurrentComponentId(sComponent);
        newInfos.setCurrentComponentName(componentInst.getName());
        newInfos.setCurrentComponentLabel(sCurCompoLabel);
        newInfos.setCurrentProfile(getAdminService().getCurrentProfiles(this.getUserId(),
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
   *
   * @deprecated
   */
  public void updateUserSpace(String sSpaceId) {
    m_CurrentSpaceId = sSpaceId;
  }

  /**
   * Update the current component for the current user
   *
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
    AdminUserConnections auc = new AdminUserConnections(userId, sessionId);
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

  public void remove() {
    if (clipboard != null) {
      clipboard.remove();
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
  public Clipboard create(final String name) {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.CLIPBOARD_EJBHOME, Clipboard.class).create(name);
  }

  @Override
  public void add(ClipboardSelection clipObject) throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.add(clipObject);
    }
  }

  @Override
  public ClipboardSelection getObject() {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getObject();
    }
  }

  @Override
  public void PasteDone() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.PasteDone();
    }
  }

  @Override
  public Collection<ClipboardSelection> getSelectedObjects() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getSelectedObjects();
    }
  }

  @Override
  public Collection<ClipboardSelection> getObjects() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getObjects();
    }
  }

  @Override
  public int size() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.size();
    }
  }

  @Override
  public ClipboardSelection getObject(int index) throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getObject(index);
    }
  }

  @Override
  public void setSelected(int index, boolean setIt) throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.setSelected(index, setIt);
    }
  }

  @Override
  public void removeObject(int index) throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.removeObject(index);
    }
  }

  @Override
  public void clear() {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.clear();
    }
  }

  @Override
  public void setMultiClipboard() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.setMultiClipboard();
    }
  }

  @Override
  public void setSingleClipboard() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.setSingleClipboard();
    }
  }

  @Override
  public String getName() {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getName();
    }
  }

  @Override
  public int getCount() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getCount();
    }
  }

  @Override
  public String getMessageError() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getMessageError();
    }
  }

  @Override
  public Exception getExceptionError() throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      return clipboard.getExceptionError();
    }
  }

  @Override
  public void setMessageError(String messageID, Exception e) throws ClipboardException {
    Clipboard clipboard = getClipboard();
    synchronized (clipboard) {
      clipboard.setMessageError(messageID, e);
    }
  }
}
