/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.web.mvc.controller;

import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.web.mvc.util.AlertUser;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.web.panel.GenericPanel;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.service.Clipboard;
import org.silverpeas.core.clipboard.service.MainClipboard;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcSettings;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.subscription.SubscriptionContext;

import javax.enterprise.util.AnnotationLiteral;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;

/*
 This object is used by all the components jsp that have access to the session.
 It is initialized given a login and a password which is authenticated.
 It provides functions to get information about the logged user (which is unique).
 It is also used to update the current environnement of the user (current domain, current component).
 */
public class MainSessionController implements Clipboard {

  public static final String MAIN_SESSION_CONTROLLER_ATT = "SilverSessionController";
  private Clipboard clipboard = ServiceProvider.getService(Clipboard.class,
      new AnnotationLiteral<MainClipboard>() {});
  private final UserPreferences userPreferences;
  private PdcManager pdcManager = null;
  private String sessionId = null;
  private String userId = null;
  private OrganizationController organizationController = null;
  private String userLanguage = null;
  private ContentManager contentManager = null;
  private Map<String, GenericPanel> genericPanels = Collections.synchronizedMap(
      new HashMap<>());
  private Selection selection = null;
  private String userSpace = null;
  private AlertUser m_alertUser = null;
  private String serverName = null;
  private String serverPort = null;
  private SubscriptionContext subscriptionContext = null;
  /**
   * Maintenance Mode *
   */
  private static boolean appInMaintenance = false;
  private static List<String> spacesInMaintenance = new ArrayList<>();
  // Last results from search engine
  private List<GlobalSilverContent> lastResults = null;
  private boolean allowPasswordChange;

  public final boolean isAppInMaintenance() {
    return appInMaintenance;
  }

  public void setAppModeMaintenance(boolean mode) {

    appInMaintenance = mode;
  }

  public boolean isSpaceInMaintenance(String spaceId) {
    spaceId = checkSpaceId(spaceId);
    boolean inMaintenance = spacesInMaintenance.contains(spaceId);

    return inMaintenance;
  }

  public void setSpaceModeMaintenance(String spaceId, boolean mode) {
    spaceId = checkSpaceId(spaceId);

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
    if (spaceId != null && spaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX)) {
      return spaceId.substring(SpaceInst.SPACE_KEY_PREFIX.length(), spaceId.length());
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
    try {
      // Identify the user
      this.userId = getAdminService().identify(authenticationKey, sessionId, isAppInMaintenance());
      this.sessionId = sessionId;
      this.userPreferences = PersonalizationServiceProvider.getPersonalizationService()
          .getUserSettings(userId);

      // Get the user language
      userLanguage = userPreferences.getLanguage();
    } catch (Exception e) {
      throw new PeasCoreException("MainSessionController.MainSessionController",
          SilverpeasException.ERROR, "peasCore.EX_CANT_GET_USER_PROFILE",
          "authenticationKey=" + authenticationKey, e);
    }
  }

  public String getClipboardName() {
    return MainClipboard.class.getSimpleName();
  }

  public String getUserId() {
    return userId;
  }

  public String getSessionId() {
    return sessionId;
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

  /**
   * Return the clipboard service
   */
  private Clipboard getClipboard() {
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
      String[] availableSpaces = getUserAvailSpaceIds();
      if (StringUtil.isDefined(userSpace)) {
        // check if this space always exist and if the user have the right to access to it
        for (String availableSpaceId : availableSpaces) {
          if (userSpace.equals(availableSpaceId)) {
            // the user is allowed to access to this space
            allowed = true;
            break;
          }
        }
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

  public boolean isWebDAVEditingEnabled() {
    return userPreferences.isWebdavEditionEnabled();
  }

  public boolean isDragNDropEnabled() {
    return userPreferences.isDragAndDropEnabled();
  }

  // ------------------- Other functions -----------------------------
  private OrganizationController getOrganisationController() {
    if (organizationController == null) {
      organizationController = OrganizationControllerProvider.getOrganisationController();
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
      return getPdcManager().isUserManager(userId);
    } catch (PdcException e) {
      SilverTrace.error("peasCore", "MainSessionController.isPDCBackOfficeVisible",
          "admin.MSG_ERR_GET_PDC_VISIBILITY", e);
    }

    return false;
  }

  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = new GlobalPdcManager();
    }

    return pdcManager;
  }

  public List<String> getUserManageableGroupIds() {
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
        String sCurCompoLabel;
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
  public void add(ClipboardSelection clipObject) throws ClipboardException {
    Clipboard clipboard = getClipboard();
      clipboard.add(clipObject);
  }

  @Override
  public ClipboardSelection getObject() {
    Clipboard clipboard = getClipboard();
      return clipboard.getObject();
  }

  @Override
  public void PasteDone() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      clipboard.PasteDone();
  }

  @Override
  public Collection<ClipboardSelection> getSelectedObjects() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      return clipboard.getSelectedObjects();
  }

  @Override
  public Collection<ClipboardSelection> getObjects() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      return clipboard.getObjects();
  }

  @Override
  public int size() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      return clipboard.size();
  }

  @Override
  public ClipboardSelection getObject(int index) throws ClipboardException {
    Clipboard clipboard = getClipboard();
      return clipboard.getObject(index);
  }

  @Override
  public void setSelected(int index, boolean setIt) throws ClipboardException {
    Clipboard clipboard = getClipboard();
      clipboard.setSelected(index, setIt);
  }

  @Override
  public void removeObject(int index) throws ClipboardException {
    Clipboard clipboard = getClipboard();
      clipboard.removeObject(index);
  }

  @Override
  public void clear() {
    Clipboard clipboard = getClipboard();
      clipboard.clear();
  }

  @Override
  public void setMultiClipboard() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      clipboard.setMultiClipboard();
  }

  @Override
  public void setSingleClipboard() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      clipboard.setSingleClipboard();
  }

  @Override
  public int getCount() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      return clipboard.getCount();
  }

  @Override
  public String getMessageError() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      return clipboard.getMessageError();
  }

  @Override
  public Exception getExceptionError() throws ClipboardException {
    Clipboard clipboard = getClipboard();
      return clipboard.getExceptionError();
  }

  @Override
  public void setMessageError(String messageID, Exception e) throws ClipboardException {
    Clipboard clipboard = getClipboard();
      clipboard.setMessageError(messageID, e);
  }
}
