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
package org.silverpeas.core.admin.space;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;

public class PersonalSpaceManager {

  private static final String MESSAGES_LOCATION =
      "org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle";

  protected PersonalSpaceManager() {
  }

  public static PersonalSpaceManager get() {
    return ServiceProvider.getService(PersonalSpaceManager.class);
  }

  /**
   * Gets visible personal component instances linked to the given user.
   * @param user a silverpeas user.
   * @return list of personal component instance.
   */
  public List<PersonalComponentInstance> getVisiblePersonalComponentInstances(User user) {
    Collection<PersonalComponent> personalComponents = PersonalComponent.getAll();
    return personalComponents.stream().filter(PersonalComponent::isVisible)
        .map(personalComponent -> PersonalComponentInstance.from(user, personalComponent))
        .collect(Collectors.toList());
  }

  public List<WAComponent> getVisibleComponents() {
    Collection<WAComponent> components = WAComponent.getAll();
    return components.stream().filter(WAComponent::isVisibleInPersonalSpace)
        .collect(Collectors.toList());
  }

  /**
   * Gets the personal space instance linked to the given user.
   * <p>If it does not yet exist, then it is created.</p>
   * @param user a user.
   * @return the {@link SpaceInst} instance.
   */
  public SpaceInst getOrCreatePersonalSpace(final User user) {
    SpaceInst space = getPersonalSpace(user.getId());
    if (space == null) {
      String userId = user.getId();
      // if user has no personal space, creates one
      space = new SpaceInst();
      space.setCreatorUserId(userId);
      space.setInheritanceBlocked(true);
      space.setLevel(0);
      space.setName("Personal space of user #" + userId);
      space.setPersonalSpace(true);

      // user is admin on space to be admin on each components
      SpaceProfileInst profile = new SpaceProfileInst();
      profile.setName("admin");
      profile.addUser(userId);
      profile.setInherited(false);
      space.getAllSpaceProfilesInst().add(profile);

      // add component to space
      try {
        getAdminService().addSpaceInst(userId, space);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
    return space;
  }

  public String addComponent(User user, String componentName)
      throws AdminException, QuotaException {
    Optional<WAComponent> optionalWAComponent = WAComponent.getByName(componentName);
    if (!optionalWAComponent.isPresent() || !optionalWAComponent.get().isVisibleInPersonalSpace()) {
      LocalizationBundle messages = getMessages(user.getUserPreferences().getLanguage());
      String errorText = messages.getString("JSPP.ErrorUnknownComponent");
      throw new AdminException(MessageFormat.format(errorText, componentName));
    }
    WAComponent wac = optionalWAComponent.get();
    String userId = user.getId();
    ComponentInst component = new ComponentInst();
    component.setCreatorUserId(userId);
    component.setInheritanceBlocked(false);
    component.setName(wac.getName());
    component.setLabel(wac.getLabel(DisplayI18NHelper.getDefaultLanguage()));
    List<Parameter> parameters = wac.getAllParameters();

    // set specific parameter values for personal space context
    for (Parameter parameter : parameters) {
      if (StringUtil.isDefined(parameter.getPersonalSpaceValue())) {
        parameter.setValue(parameter.getPersonalSpaceValue());
      }
    }
    component.setParameters(parameters);

    SpaceInst space = getOrCreatePersonalSpace(user);
    // add component in personal space
    component.setDomainFatherId(space.getId());
    getAdminService().addComponentInst(userId, component);
    // returning the identifier of the component instance
    return component.getId();
  }

  public String removeComponent(String userId, String componentId) throws AdminException {
    SpaceInst space = getPersonalSpace(userId);
    if (space != null) {
      ComponentInst component = getComponent(space, componentId);
      if (component != null) {
        getAdminService().deleteComponentInst(userId, componentId, true);
        return component.getName();
      }
    }
    return null;
  }

  public SpaceInst getPersonalSpace(String userId) {
    try {
      return getAdminService().getPersonalSpace(userId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).warn(e);
      return null;
    }
  }

  private ComponentInst getComponent(SpaceInst space, String componentId) {
    List<ComponentInst> components = space.getAllComponentsInst();
    for (ComponentInst component : components) {
      if (component.getId().equalsIgnoreCase(componentId)) {
        return component;
      }
    }
    return null;
  }

  private LocalizationBundle getMessages(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_LOCATION, language);
  }
}
