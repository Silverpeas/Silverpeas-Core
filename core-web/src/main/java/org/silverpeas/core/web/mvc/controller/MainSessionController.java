/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.mvc.controller;

import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.service.Clipboard;
import org.silverpeas.core.clipboard.service.MainClipboard;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcSettings;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.session.SessionCloseable;
import org.silverpeas.core.web.subscription.SubscriptionContext;

import javax.enterprise.util.AnnotationLiteral;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;

/*
 This object is used by all the components jsp that have access to the session.
 It is initialized given a login and a password which is authenticated.
 It provides functions to get information about the logged user (which is unique).
 It is also used to update the current environnement of the user (current domain, current component).
 */
public class MainSessionController implements Clipboard, SessionCloseable, Serializable {

  public static final String MAIN_SESSION_CONTROLLER_ATT = "SilverSessionController";
  private transient Clipboard clipboard = ServiceProvider.getService(Clipboard.class,
      new AnnotationLiteral<MainClipboard>() {});
  private final UserPreferences userPreferences;
  private transient PdcManager pdcManager = null;
  private transient HttpSession httpSession = null;
  private String sessionId = null;
  private String userId = null;
  private transient OrganizationController organizationController = null;
  private String userLanguage = null;
  private ZoneId userZoneId = null;
  private transient Selection selection = null;
  private String userSpace = null;
  private String serverName = null;
  private String serverPort = null;
  private transient SubscriptionContext subscriptionContext = null;
  /**
   * Maintenance Mode *
   */
  private static boolean appInMaintenance = false;
  private static List<String> spacesInMaintenance = new ArrayList<>();
  // Last results from search engine
  private transient List<GlobalSilverContent> lastResults = null;
  private boolean allowPasswordChange;

  public static boolean isAppInMaintenance() {
    return appInMaintenance;
  }

  public static void setAppModeMaintenance(boolean mode) {
    appInMaintenance = mode;
  }

  public boolean isSpaceInMaintenance(String spaceId) {
    String checkedSpaceId = checkSpaceId(spaceId);
    return spacesInMaintenance.contains(checkedSpaceId);
  }

  public void setSpaceModeMaintenance(String spaceId, boolean mode) {
    String checkedSpaceId = checkSpaceId(spaceId);

    if (mode) {
      if (!spacesInMaintenance.contains(checkedSpaceId)) {
        spacesInMaintenance.add(checkedSpaceId);
      }
    } else {
      if (spacesInMaintenance.contains(checkedSpaceId)) {
        spacesInMaintenance.remove(spacesInMaintenance.indexOf(checkedSpaceId));
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
   * Creates a new instance of {@link MainSessionController} related to the user which has just
   * connected to Silverpeas.
   * @param authenticationKey the authentication key of the user.
   * @param httpSession the http session.
   * @throws org.silverpeas.core.SilverpeasException if the user session is not open.
   */
  public MainSessionController(String authenticationKey, HttpSession httpSession)
      throws org.silverpeas.core.SilverpeasException {
    try {
      this.httpSession = httpSession;
      this.sessionId = httpSession.getId();
      httpSession.setAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT, this);
      // Identify the user
      this.userId = getAdminService().identify(authenticationKey, sessionId, isAppInMaintenance());
      this.userPreferences = PersonalizationServiceProvider.getPersonalizationService()
          .getUserSettings(userId);

      // Get the user language
      userLanguage = userPreferences.getLanguage();
    } catch (Exception e) {
      throw new org.silverpeas.core.SilverpeasException(
          failureOnGetting("user with authentication key", authenticationKey));
    }
  }

  /**
   * Creates a new instance of {@link MainSessionController} related to the user already
   * connected to Silverpeas.
   * @param sessionInfo an existing session info.
   * @param httpSession the http session.
   * @throws org.silverpeas.core.SilverpeasException if the user session is not open.
   */
  public MainSessionController(SessionInfo sessionInfo, HttpSession httpSession)
      throws org.silverpeas.core.SilverpeasException {
    try {
      this.httpSession = httpSession;
      this.sessionId = httpSession.getId();
      httpSession.setAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT, this);
      // Identify the user
      this.userId = sessionInfo.getUserDetail().getId();
      this.userPreferences =
          PersonalizationServiceProvider.getPersonalizationService().getUserSettings(userId);

      // Get the user language
      userLanguage = userPreferences.getLanguage();
    } catch (Exception e) {
      throw new org.silverpeas.core.SilverpeasException(
          "can not initialize main controller from session id {0}", sessionInfo.getSessionId());
    }
  }

  public String getUserId() {
    return userId;
  }

  public HttpSession getHttpSession() {
    return httpSession;
  }

  public String getSessionId() {
    return sessionId;
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
   * Return the user's favorite zone identifier
   */
  public ZoneId getFavoriteZoneId() {
    if (userZoneId== null) {
      userZoneId = userPreferences.getZoneId();
    }
    return userZoneId;
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

  public synchronized void setFavoriteSpace(String newSpace) {
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
      SilverLogger.getLogger(this).error(failureOnGetting("user", getUserId()), e);
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
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new String[0];
    }
  }

  /**
   * Return the spaces ids available for the current user
   */
  public String[] getUserAvailSpaceIds() {

    try {
      return getAdminService().getClientSpaceIds(getAdminService().getUserSpaceIds(userId));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new String[0];
    }
  }

  /**
   * Return the spaces ids manageable by the current user
   */
  public String[] getUserManageableSpaceIds() {
    try {
      UserDetail user = getAdminService().getUserDetail(userId);
      if (user.isAccessAdmin() || "0".equals(userId)) {
        return getAdminService().getClientSpaceIds(getAdminService().getAllSpaceIds());
      } else {
        return getAdminService().getClientSpaceIds(getAdminService().getUserManageableSpaceIds(
            userId));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new String[0];
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
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }

    return false;
  }

  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = PdcManager.get();
    }

    return pdcManager;
  }

  public List<String> getUserManageableGroupIds() {
    try {
      return getAdminService().getUserManageableGroupIds(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  /**
   * Helper function. Create a new CurrentSessionControl object and fill it with the values of the
   * current space Id and component instance Id passed in parameters
   */
  @SuppressWarnings("ConstantConditions")
  public ComponentContext createComponentContext(String spaceId, String componentInstanceId) {
    ComponentContext componentContext = new ComponentContext();

    try {
      // Set the space
      if (spaceId != null) {
        SpaceInstLight spaceInst = getAdminService().getSpaceInstLightById(spaceId);

        componentContext.setCurrentSpaceId(spaceId);
        componentContext.setCurrentSpaceName(spaceInst.getName(getFavoriteLanguage()));
      }
      // Set the current component and profiles
      if (componentInstanceId != null) {
        final SilverpeasComponentInstance componentInst =
            getAdminService().getComponentInstance(componentInstanceId);
        componentContext.setCurrentComponentId(componentInstanceId);
        componentContext.setCurrentComponentName(componentInst.getName());
        if (componentInst.isPersonal()) {
          Collection<SilverpeasRole> silverpeasRolesFor =
              componentInst.getSilverpeasRolesFor(getCurrentUserDetail());
          componentContext.setCurrentProfile(
              silverpeasRolesFor.stream().map(SilverpeasRole::getName).toArray(String[]::new));
        } else {
          componentContext.setCurrentProfile(
              getAdminService().getCurrentProfiles(this.getUserId(), componentInst.getId()));
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("can not create component context with spaceId={0} and componentId={1}",
              new String[]{spaceId, componentInstanceId}, e);
    }
    return componentContext;
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
    getClipboard().add(clipObject);
  }

  @Override
  public ClipboardSelection getObject() {
    return getClipboard().getObject();
  }

  @Override
  public void PasteDone() throws ClipboardException {
    getClipboard().PasteDone();
  }

  @Override
  public Collection<ClipboardSelection> getSelectedObjects() throws ClipboardException {
    return getClipboard().getSelectedObjects();
  }

  @Override
  public Collection<ClipboardSelection> getObjects() throws ClipboardException {
    return getClipboard().getObjects();
  }

  @Override
  public int size() throws ClipboardException {
    return getClipboard().size();
  }

  @Override
  public ClipboardSelection getObject(int index) throws ClipboardException {
    return getClipboard().getObject(index);
  }

  @Override
  public void setSelected(int index, boolean setIt) throws ClipboardException {
    getClipboard().setSelected(index, setIt);
  }

  @Override
  public void removeObject(int index) throws ClipboardException {
    getClipboard().removeObject(index);
  }

  @Override
  public void close() {
    clear();
  }

  @Override
  public void clear() {
    getClipboard().clear();
  }

  @Override
  public void setMultiClipboard() throws ClipboardException {
    getClipboard().setMultiClipboard();
  }

  @Override
  public void setSingleClipboard() throws ClipboardException {
    getClipboard().setSingleClipboard();
  }

  @Override
  public int getCount() throws ClipboardException {
    return getClipboard().getCount();
  }

  @Override
  public String getMessageError() throws ClipboardException {
    return getClipboard().getMessageError();
  }

  @Override
  public Exception getExceptionError() throws ClipboardException {
    return getClipboard().getExceptionError();
  }

  @Override
  public void setMessageError(String messageID, Exception e) throws ClipboardException {
    getClipboard().setMessageError(messageID, e);
  }
}
