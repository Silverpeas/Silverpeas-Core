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

package org.silverpeas.core.web.mvc.controller;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.web.mvc.util.AlertUser;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.web.panel.GenericPanel;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.admin.component.constant.ComponentInstanceParameterName;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.web.subscription.SubscriptionContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;



/**
 * Base class for all component session controller.
 */
public class AbstractComponentSessionController implements ComponentSessionController {

  /**
   * The default character encoded supported by Silverpeas.
   */
  public static final String CHARACTER_ENCODING = "UTF-8";
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

  /**
   * Constructor declaration
   * @param controller
   * @param spaceId
   * @param componentId
   * @see
   */
  public AbstractComponentSessionController(MainSessionController controller,
      String spaceId, String componentId) {
    this.controller = controller;
    this.context = controller.createComponentContext(spaceId, componentId);
    setComponentRootName(URLUtil.getComponentNameFromComponentId(componentId));
  }

  /**
   * Constructor declaration
   * @param controller
   * @param context
   * @see
   */
  public AbstractComponentSessionController(MainSessionController controller,
      ComponentContext context) {
    this(controller, context, null);
  }

  /**
   * Constructor declaration
   * @param controller
   * @param context
   * @param resourceFileName
   * @see
   */
  public AbstractComponentSessionController(MainSessionController controller,
      ComponentContext context, String resourceFileName) {
    this.controller = controller;
    this.context = context;
    setComponentRootName(URLUtil.getComponentNameFromComponentId(getComponentId()));
    setResourceFileName(resourceFileName);
  }

  public AbstractComponentSessionController(MainSessionController controller,
      ComponentContext context, String multilangFileName, String iconFileName) {
    this.controller = controller;
    this.context = context;
    setComponentRootName(URLUtil.getComponentNameFromComponentId(getComponentId()));
    setMultilangFileName(multilangFileName);
    setIconFileName(iconFileName);
  }

  public AbstractComponentSessionController(MainSessionController controller,
      ComponentContext context, String multilangFileName, String iconFileName,
      String settingsFileName) {
    this.controller = controller;
    this.context = context;
    setComponentRootName(URLUtil.getComponentNameFromComponentId(getComponentId()));
    setMultilangFileName(multilangFileName);
    setIconFileName(iconFileName);
    this.settingsFile = settingsFileName;
  }

  @Override
  public LocalizationBundle getMultilang() {

    if (message != null && !message.getLocale().getLanguage().equals(
        controller.getFavoriteLanguage())) {
      setMultilangFileName(messageFile);
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

  /**
   * Method declaration
   * @param multilangFileName
   */
  public final void setMultilangFileName(String multilangFileName) {
    messageFile = multilangFileName;
    if (messageFile != null) {
      try {
        messageLanguage = getLanguage();
        message = ResourceLocator.getLocalizationBundle(messageFile, messageLanguage);
        // messageLanguage = getLanguage();

      } catch (Exception e) {
        SilverTrace.error("peasCore", "AbstractComponentSessionController.setResourceFileName()",
            "root.EX_CANT_GET_LANGUAGE_RESOURCE", "File=" + messageFile + "|Language="
            + getLanguage(), e);
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
  public final void setIconFileName(String iconFileName) {
    iconFile = iconFileName;
    if (iconFile != null) {
        icon = ResourceLocator.getSettingBundle(iconFile);
    } else {
      icon = null;
    }
  }

  /**
   * Method declaration
   * @param resourceFileName
   * @see
   */
  public final void setResourceFileName(String resourceFileName) {

    messageFile = resourceFileName;
    if (messageFile != null) {
      try {
        messageLanguage = getLanguage();
        message = ResourceLocator.getLocalizationBundle(messageFile, messageLanguage);

      } catch (Exception e) {
        SilverTrace.error("peasCore", "AbstractComponentSessionController.setResourceFileName()",
            "root.EX_CANT_GET_LANGUAGE_RESOURCE", "File=" + messageFile
            + "|Language=" + getLanguage(), e);
        message = ResourceLocator.getLocalizationBundle(messageFile);
        messageLanguage = getLanguage();
      }
    } else {
      message = null;
    }
  }

  /**
   * Method declaration
   * @param resName
   * @return
   * @see
   */
  public String getString(String resName) {
    String theLanguage = getLanguage();
    if ((theLanguage != null) || (message == null)) {
      if (message == null || messageLanguage == null || !messageLanguage.equals(theLanguage)) {
        setResourceFileName(messageFile);
      }
    }
    if (message == null) {
      return resName;
    }
    return message.getString(resName);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
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

  public String getFavoriteSpace() {
    return controller.getFavoriteSpace();
  }

  /**
   * The utilization of this method is allowed only for PersonalizationSessionController
   * @param newSpace the new user favorite space.
   */
  public void setFavoriteSpaceToMainSessionController(String newSpace) {
    controller.setFavoriteSpace(newSpace);
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
    return context.getCurrentComponentLabel();
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
   * return the component Root name : i.e. 'agenda', 'todo', 'kmelia', .... (the name that appears
   * in the URL's root (the 'R' prefix is added later when needed))
   * @return the component root name.
   */
  public final String getComponentRootName() {
    return rootName;
  }

  /**
   * Sets the component root name : i.e. 'agenda', 'todo', 'kmelia', .... (the name that appears in
   * the URL's root (the 'R' prefix is added later when needed)) this function is called by the
   * class of non-instanciable components the inherits from this class
   * @param newRootName the new root component.
   */
  protected final void setComponentRootName(String newRootName) {
    rootName = newRootName;
  }

  /**
   * Gets the parameters for the current component instance.
   * @return a list of current component parameters.
   */
  @Override
  public List<Parameter> getComponentParameters() {
    return controller.getComponentParameters(getComponentId());
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
   * Gets the user's available workspace.
   * @return an array with all available spaces identifiers.
   */
  @Override
  public String[] getUserAvailSpaceIds() {
    return controller.getUserAvailSpaceIds();
  }

  public String[] getUserManageableSpaceIds() {
    return controller.getUserManageableSpaceIds();
  }

  public List<String> getUserManageableGroupIds() {
    return controller.getUserManageableGroupIds();
  }

  public boolean isGroupManager() {
    return !getUserManageableGroupIds().isEmpty();
  }

  /**
   * Gets all of the roles the current user plays in Silverpeas.
   * @return an array with all the user role names.
   */
  @Override
  @Deprecated
  public String[] getUserRoles() {
    return context.getCurrentProfile();
  }

  /**
   * Gets the highest privileged role the current user can play (administrator, publisher or user).
   * @return the highest privileged role name of the current user.
   */
  @Override
  @Deprecated
  public String getUserRoleLevel() {
    String[] profiles = getUserRoles();
    String flag = SilverpeasRole.user.toString();

    for (String profile : profiles) {
      // if admin, return it, we won't find a better profile
      if (SilverpeasRole.admin.isInRole(profile)) {
        return profile;
      }
      if (SilverpeasRole.publisher.isInRole(profile)) {
        flag = profile;
      }
    }
    return flag;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<SilverpeasRole> getSilverpeasUserRoles() {
    String currentKey = getComponentId() + "_user_roles";
    Collection<SilverpeasRole> roles =
        (Collection<SilverpeasRole>) CacheServiceProvider.getRequestCacheService().get(currentKey);
    if (roles == null) {
      roles = SilverpeasRole.from(context.getCurrentProfile());
      roles.remove(SilverpeasRole.Manager);
      CacheServiceProvider.getRequestCacheService().put(currentKey, roles);
    }
    return roles;
  }

  @Override
  public SilverpeasRole getHighestSilverpeasUserRole() {
    SilverpeasRole highestUserRole = SilverpeasRole.getGreaterFrom(getSilverpeasUserRoles());
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

  public void setGenericPanel(String panelKey, GenericPanel panel) {
    controller.setGenericPanel(panelKey, panel);
  }

  public GenericPanel getGenericPanel(String panelKey) {
    return controller.getGenericPanel(panelKey);
  }

  public Selection getSelection() {
    return controller.getSelection();
  }

  public SubscriptionContext getSubscriptionContext() {
    return controller.getSubscriptionContext();
  }

  public AlertUser getAlertUser() {
    return controller.getAlertUser();
  }

  // Maintenance Mode
  @Override
  public boolean isAppInMaintenance() {
    return controller.isAppInMaintenance();
  }

  @Override
  public void setAppModeMaintenance(boolean mode) {
    controller.setAppModeMaintenance(mode);
  }

  @Override
  public boolean isSpaceInMaintenance(String spaceId) {
    return controller.isSpaceInMaintenance(spaceId);
  }

  @Override
  public void setSpaceModeMaintenance(String spaceId, boolean mode) {
    controller.setSpaceModeMaintenance(spaceId, mode);
  }

  public String getServerNameAndPort() {
    return controller.getServerNameAndPort();
  }

  public List<GlobalSilverContent> getLastResults() {
    return controller.getLastResults();
  }

  public void setLastResults(List<GlobalSilverContent> results) {
    controller.setLastResults(results);
  }

  public void close() {
  }

  public boolean isPasswordChangeAllowed() {
    return controller.isAllowPasswordChange();
  }

  public String getRSSUrl() {
    StringBuilder builder = new StringBuilder();
    builder.append("/rss").append(getComponentRootName()).append('/').append(getComponentId());
    builder.append("?userId=").append(getUserId()).append("&login=");
    builder.append(getUrlEncodedParameter(getUserDetail().getLogin()));
    builder.append("&password=");
    builder.append(getUrlEncodedParameter(getOrganisationController().getUserFull(
        getUserId()).getPassword()));
    return builder.toString();
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
  public String getClipboardName() {
    return controller.getClipboardName();
  }

  @Override
  public Integer getClipboardCount() throws ClipboardException {
    return controller.getCount();
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

  @Override
  public void setClipboardError(String messageId, Exception ex) throws ClipboardException {
    controller.setMessageError(messageId, ex);
  }

  protected ComponentAccessController getComponentAccessController() {
    return ServiceProvider.getService(ComponentAccessController.class);
  }
}
