/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.silverpeas.core.admin.component.constant.ComponentInstanceParameterName;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.session.SessionCloseable;
import org.silverpeas.core.web.subscription.SubscriptionContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;


/**
 * Base class for all component session controller.
 */
public abstract class AbstractComponentSessionController implements ComponentSessionController,
    SessionCloseable {

  /**
   * The default character encoded supported by Silverpeas.
   */
  private static final String CHARACTER_ENCODING = "UTF-8";
  protected ComponentContext context = null;
  private MainSessionController controller = null;
  private String rootName = null;
  private LocalizationBundle message = null;
  private SettingBundle icon = null;
  private String messageLanguage = null;
  private String messageFile = null;
  private String iconFile = null;
  private SettingBundle settings = null;
  private String settingsFile = null;

  public AbstractComponentSessionController(MainSessionController controller,
      String spaceId, String componentId) {
    this(controller, controller.createComponentContext(spaceId, componentId));
  }

  public AbstractComponentSessionController(MainSessionController controller,
      ComponentContext context) {
    this(controller, context, null);
  }

  public AbstractComponentSessionController(MainSessionController controller,
      ComponentContext context, String localizedMessagesBundleName) {
    this(controller, context, localizedMessagesBundleName, null);
  }

  public AbstractComponentSessionController(MainSessionController controller,
      ComponentContext context, String localizedMessagesBundleName, String iconFileName) {
    this(controller, context, localizedMessagesBundleName, iconFileName, null);
  }

  public AbstractComponentSessionController(MainSessionController controller,
      ComponentContext context, String localizedMessagesBundleName, String iconFileName,
      String settingsFileName) {
    this.controller = controller;
    this.context = context;
    if (StringUtil.isDefined(context.getCurrentComponentName())) {
      setComponentRootName(context.getCurrentComponentName());
    } else {
      setComponentRootName(
          SilverpeasComponentInstance.getComponentName(context.getCurrentComponentId()));
    }
    setLocalizationBundle(localizedMessagesBundleName);
    setIconFileName(iconFileName);
    this.settingsFile = settingsFileName;
  }

  @Override
  public LocalizationBundle getMultilang() {

    if (message != null && !message.getLocale().getLanguage().equals(
        controller.getFavoriteLanguage())) {
      setLocalizationBundle(messageFile);
    }
    return message;
  }

  @Override
  public SettingBundle getIcon() {
    if (icon != null) {
      setIconFileName(iconFile);
    }
    return icon;
  }

  @Override
  public SettingBundle getSettings() {
    if (settings == null && settingsFile != null) {
      settings = ResourceLocator.getSettingBundle(settingsFile);
    }
    return settings;
  }

  public String getString(String resName) {
    String theLanguage = getLanguage();
    if ((theLanguage != null || message == null) &&
        (message == null || messageLanguage == null || !messageLanguage.equals(theLanguage))) {
        setLocalizationBundle(messageFile);
    }
    if (message == null) {
      return resName;
    }
    return message.getString(resName);
  }

  @Override
  public OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  /**
   * Gets the main language of the user.
   * @return the user language code.
   */
  @Override
  public String getLanguage() {
    return controller.getFavoriteLanguage();
  }

  @Override
  public ZoneId getZoneId() {
    return controller.getFavoriteZoneId();
  }

  /**
   * Gets the identifier of the user website look.
   * @return the user favorite look name.
   */
  public String getLook() {
    return controller.getFavoriteLook();
  }

  /**
   * Gets details on the connected current user.
   * @return the UserDetail information about the current user.
   */
  @Override
  public UserDetail getUserDetail() {
    return controller.getCurrentUserDetail();
  }

  public UserDetail getUserDetail(String userId) {
    return UserDetail.getById(userId);
  }

  /**
   * Gets the unique identifier of the current connected user.
   * @return the user identifier.
   */
  @Override
  public String getUserId() {
    return getUserDetail().getId();
  }

  /**
   * Gets the space label (as known by the user).
   * @return the space label.
   */
  @Override
  public String getSpaceLabel() {
    return context.getCurrentSpaceName();
  }

  /**
   * Gets the unique identifier of the current selected workspace.
   * @return the space identifier.
   */
  @Override
  public String getSpaceId() {
    return context.getCurrentSpaceId();
  }

  @Override
  public String getComponentName() {
    return context.getCurrentComponentName();
  }

  /**
   * Gets the label of the current used component (as known by the user).
   * @return the component label.
   */
  @Override
  public final String getComponentLabel() {
    return OrganizationController.get().getComponentInstance(getComponentId())
        .map(i -> i.getLabel(getLanguage()))
        .orElse(StringUtil.EMPTY);
  }

  /**
   * Return the unique identifier of the current component.
   * @return the current component identifier.
   */
  @Override
  public final String getComponentId() {
    return context.getCurrentComponentId();
  }

  /**
   * Gets the URL at which is located the current selected component. Warning: For old components'
   * use ONLY! (use it in the jsp:forward lines).
   * @return the current component URL.
   */
  @Override
  public final String getComponentUrl() {
    return URLUtil.getURL(rootName, getSpaceId(), getComponentId());
  }

  /**
   * return the component Root name : i.e. 'calendar', 'kmelia', .... (the name that appears
   * in the URL's root (the 'R' prefix is added later when needed))
   * @return the component root name.
   */
  public final String getComponentRootName() {
    return rootName;
  }

  /**
   * Sets the component root name : i.e. 'agenda', 'calendar', 'kmelia', .... (the name that
   * appears in
   * the URL's root (the 'R' prefix is added later when needed)) this function is called by the
   * class of non-instanciable components the inherits from this class
   * @param newRootName the new root component.
   */
  protected final void setComponentRootName(String newRootName) {
    rootName = newRootName;
  }

  /**
   * Gets the value of the specified current component's parameter.
   * @param parameterName the name of the parameter to get.
   * @return the value of the parameter.
   */
  @Override
  public String getComponentParameterValue(String parameterName) {
    return controller.getComponentParameterValue(getComponentId(), parameterName);
  }

  /**
   * Gets the value of the specified current component's parameter.
   * @param parameterName the name (centralized) of the parameter to get.
   * @return the value of the parameter.
   */
  @Override
  public String getComponentParameterValue(ComponentInstanceParameterName parameterName) {
    return getComponentParameterValue(parameterName.name());
  }

  /**
   * Gets the user's available components.
   * @return an array with all available component identifiers.
   */
  @Override
  public String[] getUserAvailComponentIds() {
    return controller.getUserAvailComponentIds();
  }

  /**
   * Gets all of the roles the current user plays in Silverpeas.
   * @return an array with all the user role names.
   * @deprecated please use instead {@link #getSilverpeasUserRoles()} method.
   */
  @Override
  @Deprecated
  public String[] getUserRoles() {
    return context.getCurrentProfile();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<SilverpeasRole> getSilverpeasUserRoles() {
    String currentKey = getComponentId() + "_user_roles";
    Collection<SilverpeasRole> roles =
        (Collection<SilverpeasRole>) CacheServiceProvider.getRequestCacheService()
            .getCache()
            .get(currentKey);
    if (roles == null) {
      roles = SilverpeasRole.from(context.getCurrentProfile());
      roles.remove(SilverpeasRole.Manager);
      CacheServiceProvider.getRequestCacheService().getCache().put(currentKey, roles);
    }
    return roles;
  }

  @Override
  public SilverpeasRole getHighestSilverpeasUserRole() {
    SilverpeasRole highestUserRole = SilverpeasRole.getHighestFrom(getSilverpeasUserRoles());
    if (highestUserRole == null) {
      highestUserRole = SilverpeasRole.reader;
    }
    return highestUserRole;
  }

  @Override
  public synchronized UserPreferences getPersonalization() {
    return controller.getPersonalization();
  }

  public UserAccessLevel getUserAccessLevel() {
    return controller.getUserAccessLevel();
  }

  public Selection getSelection() {
    return controller.getSelection();
  }

  // Maintenance Mode
  @Override
  public boolean isAppInMaintenance() {
    return MainSessionController.isAppInMaintenance();
  }

  @Override
  public void setAppModeMaintenance(boolean mode) {
    MainSessionController.setAppModeMaintenance(mode);
  }

  @Override
  public boolean isSpaceInMaintenance(String spaceId) {
    return controller.isSpaceInMaintenance(spaceId);
  }

  @Override
  public void setSpaceModeMaintenance(String spaceId, boolean mode) {
    controller.setSpaceModeMaintenance(spaceId, mode);
  }

  public void close() {
  }

  public boolean isPasswordChangeAllowed() {
    return controller.isAllowPasswordChange();
  }

  public String getRSSUrl() {
    return "/rss" + getComponentRootName() + "/" + getComponentId() + "?userId=" + getUserId() +
        "&login=" + getUrlEncodedParameter(getUserDetail().getLogin()) +
        "&password=" + getUrlEncodedParameter(UserFull.getById(getUserId()).getPassword());
  }

  @Override
  public Collection<ClipboardSelection> getClipboardSelectedObjects() throws ClipboardException {
    return controller.getSelectedObjects();
  }

  @Override
  public String getClipboardErrorMessage() throws ClipboardException {
    return controller.getMessageError();
  }

  @Override
  public Exception getClipboardExceptionError() throws ClipboardException {
    return controller.getExceptionError();
  }

  @Override
  public Collection<ClipboardSelection> getClipboardObjects() throws ClipboardException {
    return controller.getObjects();
  }

  @Override
  public void addClipboardSelection(ClipboardSelection selection) throws ClipboardException {
    controller.add(selection);
  }

  @Override
  public void clipboardPasteDone() throws ClipboardException {
    controller.PasteDone();
  }

  @Override
  public void setClipboardSelectedElement(int index, boolean selected) throws ClipboardException {
    controller.setSelected(index, selected);
  }

  @Override
  public int getClipboardSize() throws ClipboardException {
    return controller.size();
  }

  @Override
  public void removeClipboardElement(int index) throws ClipboardException {
    controller.removeObject(index);
  }

  protected String[] getUserManageableSpaceIds() {
    return controller.getUserManageableSpaceIds();
  }

  protected List<String> getUserManageableGroupIds() {
    return controller.getUserManageableGroupIds();
  }

  protected boolean isGroupManager() {
    return !getUserManageableGroupIds().isEmpty();
  }

  protected SubscriptionContext getSubscriptionContext() {
    return controller.getSubscriptionContext();
  }

  /**
   * Gets the URL encoded representation of the specified parameter.
   * @param param the parameter.
   * @return a URL encoded representation of the parameter.
   */
  protected String getUrlEncodedParameter(String param) {
    try {
      return URLEncoder.encode(param, CHARACTER_ENCODING);
    } catch (UnsupportedEncodingException ex) {
      return param;
    }
  }

  protected ComponentAccessControl getComponentAccessController() {
    return ComponentAccessControl.get();
  }

  private void setLocalizationBundle(String bundleName) {
    messageFile = bundleName;
    if (messageFile != null) {
      try {
        messageLanguage = getLanguage();
        message = ResourceLocator.getLocalizationBundle(messageFile, messageLanguage);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Localization bundle '" + messageFile +
            "' not found for language " + messageLanguage, e);
        message = ResourceLocator.getLocalizationBundle(messageFile);
        messageLanguage = getLanguage();
      }
    } else {
      message = null;
    }
  }

  /**
   * Sets the icon file by its name. The icon file is a file in which is stored the icon that
   * represents the underlying Silverpeas component this controller works with.
   * @param iconFileName the name of the icon file.
   */
  private void setIconFileName(String iconFileName) {
    iconFile = iconFileName;
    if (iconFile != null) {
      icon = ResourceLocator.getSettingBundle(iconFile);
    } else {
      icon = null;
    }
  }
}
