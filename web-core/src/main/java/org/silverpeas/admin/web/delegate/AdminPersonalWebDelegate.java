/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.admin.web.delegate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.silverpeas.admin.web.tools.AbstractTool;
import org.silverpeas.admin.web.tools.ToolDelegate;
import org.silverpeas.look.web.delegate.LookWebDelegate;

import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import com.stratelia.webactiv.beans.admin.PersonalSpaceController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

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
   * @return
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
   * Gets all components that can be handled in personnal space but not used yet.
   * @return
   */
  public Collection<WAComponent> getNotUsedComponents() {
    return getCachedNotUsedComponents().values();
  }

  /**
   * Gets used components instanciated in the user personal space.
   * @return
   */
  public Collection<ComponentInst> getUsedComponents() {
    return getCachedUsedComponents().values();
  }

  /**
   * Gets used tools in the user personal space.
   * @return
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
        component.getName() + component.getId());

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
      indexedNotUsedComponents = new LinkedHashMap<String, WAComponent>();
      for (final WAComponent component : getPersonalSpaceController().getVisibleComponents(
          getOrganizationController())) {
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
      indexedUsedComponents = new LinkedHashMap<String, ComponentInst>();
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
      indexedUsedTools = new LinkedHashMap<String, AbstractTool>();
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
   * Indicates if the WAComponent is already used in the user personal space or not.
   * @param component
   * @return
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
   * @param user
   * @param userPreference
   * @param lookDelegate
   * @return
   */
  public static AdminPersonalWebDelegate getInstance(final UserDetail user,
      final UserPreferences userPreference, final LookWebDelegate lookDelegate) {
    return new AdminPersonalWebDelegate(user, userPreference, lookDelegate);
  }

  /**
   * Hidden constructor.
   * @param user
   * @param userPreference
   * @param lookDelegate
   */
  private AdminPersonalWebDelegate(final UserDetail user, final UserPreferences userPreference,
      final LookWebDelegate lookDelegate) {
    this.user = user;
    this.userPreference = userPreference;
    this.lookDelegate = lookDelegate;
  }

  /**
   * @return
   */
  private String getUserId() {
    return user.getId();
  }

  private OrganizationController getOrganizationController() {
    if (organizationController == null) {
      organizationController =
          OrganizationControllerFactory.getFactory().getOrganizationController();
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
      psc = new PersonalSpaceController();
    }
    return psc;
  }
}
