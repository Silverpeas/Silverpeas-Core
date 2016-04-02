/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.webapi.admin.delegate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.webapi.admin.tools.AbstractTool;
import org.silverpeas.core.webapi.admin.tools.ToolDelegate;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.webapi.look.delegate.LookWebDelegate;

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.PersonalSpaceController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.exception.SilverpeasException;

/**
 * @author Yohann Chastagnier
 */
public class AdminPersonalWebDelegate {

  private OrganizationController organizationController;

  private final UserDetail user;
  private final UserPreferences userPreference;

  private final LookWebDelegate lookDelegate;
  private ToolDelegate toolDelegate;

  private PersonalSpaceController psc = null;
  private Map<String, WAComponent> indexedNotUsedComponents = null;
  private Map<String, ComponentInst> indexedUsedComponents = null;
  private Map<String, AbstractTool> indexedUsedTools = null;

  /**
   * Gets the WAComponent label
   * @param component
   * @return the component label
   */
  public String getComponentLabel(final WAComponent component) {
    String label =
        getLookDelegate().getHelper().getString(
            "lookSilverpeasV5.personalSpace." + component.getName());
    if (!StringUtil.isDefined(label)) {
      label = component.getName();
    }
    return label;
  }

  /**
   * @return all components that can be handled in personnal space but not used yet.
   */
  public Collection<WAComponent> getNotUsedComponents() {
    return getCachedNotUsedComponents().values();
  }

  /**
   * @return used components instanciated in the user personal space.
   */
  public Collection<ComponentInst> getUsedComponents() {
    return getCachedUsedComponents().values();
  }

  /**
   * @return used tools in the user personal space.
   */
  public Collection<AbstractTool> getUsedTools() {
    return getCachedUsedTools().values();
  }

  /**
   * Instantiates the requested component in the user's personal space
   * @param componentName the WAComponent name
   * @return the instantiated component
   * @throws Exception
   */
  public ComponentInst useComponent(final String componentName) throws Exception {

    // Retrieving the WAComponent by the given component name
    final WAComponent component = getCachedNotUsedComponents().get(componentName.toLowerCase());

    // Unknown or already used component
    if (component == null) {
      throw new AdminException("AdminPersonalWebDelegate.useComponent", SilverpeasException.ERROR,
          "root.EX_UNKNOWN_COMPONENT_OR_COMPONENT_ALREADY_USED");
    }

    // User's component registration
    getPersonalSpaceController().addComponent(getUserId(), component.getName(),
        getComponentLabel(component));

    // Returning the instatiated component
    clearCache();
    return getCachedUsedComponents().get(componentName.toLowerCase());
  }

  /**
   * Deletes from the user's personal space the instantiation of the requested component
   * @param componentName the WAComponent name
   * @return the WAComponent
   * @throws Exception
   */
  public WAComponent discardComponent(final String componentName) throws Exception {

    // Retrieving the instantiated component by the given component name
    final ComponentInst component = getCachedUsedComponents().get(componentName.toLowerCase());

    // Unknown used component
    if (component == null) {
      throw new AdminException("AdminPersonalWebDelegate.discardComponent",
          SilverpeasException.ERROR, "root.EX_UNKNOWN_COMPONENT_ID");
    }

    // User's component unregistration
    getPersonalSpaceController().removeComponent(getUserId(),
        component.getId());

    // Returning the instatiated component
    clearCache();
    return getCachedNotUsedComponents().get(componentName.toLowerCase());
  }

  /**
   * Gets all components that can be handled in personnal space but not used yet.
   * Notice that components of this result are cached at this method call as this set of
   * delegate methods is instantiated for each web service http request.
   * @return
   */
  private Map<String, WAComponent> getCachedNotUsedComponents() {
    if (indexedNotUsedComponents == null) {
      indexedNotUsedComponents = new LinkedHashMap<>();
      for (final WAComponent component : getPersonalSpaceController().getVisibleComponents(
          getOrganisationController())) {
        if (!isComponentUsed(component)) {
          indexedNotUsedComponents.put(component.getName().toLowerCase(), component);
        }
      }
    }
    return indexedNotUsedComponents;
  }

  /**
   * Gets used components instanciated in the user personal space.
   * Notice that components of this result are cached at this method call as this set of
   * delegate methods is instantiated for each web service http request.
   * @return
   */
  private Map<String, ComponentInst> getCachedUsedComponents() {
    if (indexedUsedComponents == null) {
      indexedUsedComponents = new LinkedHashMap<>();
      final SpaceInst space = getPersonalSpaceController().getPersonalSpace(getUserId());
      if (space != null) {
        for (final ComponentInst component : space.getAllComponentsInst()) {
          indexedUsedComponents.put(component.getName().toLowerCase(), component);
        }
      }
    }
    return indexedUsedComponents;
  }

  /**
   * Gets used tools in the user personal space.
   * Notice that tools of this result are cached at this method call as this set of
   * delegate methods is instantiated for each web service http request.
   * @return
   */
  private Map<String, AbstractTool> getCachedUsedTools() {
    if (indexedUsedTools == null) {
      indexedUsedTools = new LinkedHashMap<>();
      if (!getLookDelegate().getHelper().isAnonymousAccess() &&
          getLookDelegate().getHelper().getSettings("personnalSpaceVisible", true)) {
        for (final AbstractTool tool : getToolDelegate().getAllTools()) {
          if (tool.isVisible()) {
            indexedUsedTools.put(tool.getId().toLowerCase(), tool);
          }
        }
      }
    }
    return indexedUsedTools;
  }

  /**
   * @param component
   * @return if the WAComponent is already used in the user personal space or not.
   */
  private boolean isComponentUsed(final WAComponent component) {
    return getCachedUsedComponents().containsKey(component.getName().toLowerCase());
  }

  /**
   * Clears all cached containers
   */
  private void clearCache() {
    indexedNotUsedComponents = null;
    indexedUsedComponents = null;
    indexedUsedTools = null;
  }

  /**
   * Easy way to instance the look service provider.
   * @param user the user detail
   * @param userPreference the user preference
   * @param lookDelegate
   * @return
   */
  public static AdminPersonalWebDelegate getInstance(final UserDetail user,
      final UserPreferences userPreference, final LookWebDelegate lookDelegate) {
    return new AdminPersonalWebDelegate(user, userPreference, lookDelegate);
  }

  /**
   * Hidden constructor.
   * @param user the user detail
   * @param userPreference the user preference
   * @param lookDelegate
   */
  private AdminPersonalWebDelegate(final UserDetail user, final UserPreferences userPreference,
      final LookWebDelegate lookDelegate) {
    this.user = user;
    this.userPreference = userPreference;
    this.lookDelegate = lookDelegate;
  }

  /**
   * @return the user identifier
   */
  private String getUserId() {
    return user.getId();
  }

  private OrganizationController getOrganisationController() {
    if (organizationController == null) {
      organizationController =
          OrganizationControllerProvider.getOrganisationController();
    }
    return organizationController;
  }

  private LookWebDelegate getLookDelegate() {
    return lookDelegate;
  }

  private ToolDelegate getToolDelegate() {
    if (toolDelegate == null) {
      toolDelegate =
          ToolDelegate.getInstance(userPreference.getLanguage(), getLookDelegate().getHelper());
    }
    return toolDelegate;
  }

  private PersonalSpaceController getPersonalSpaceController() {
    if (psc == null) {
      psc = PersonalSpaceController.getInstance();
    }
    return psc;
  }
}
