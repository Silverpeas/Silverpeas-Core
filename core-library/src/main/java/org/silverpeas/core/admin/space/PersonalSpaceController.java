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
package org.silverpeas.core.admin.space;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;

public class PersonalSpaceController {

  private static final String MESSAGES_LOCATION =
      "org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle";

  public static PersonalSpaceController getInstance() {
    return ServiceProvider.getService(PersonalSpaceController.class);
  }

  protected PersonalSpaceController() {

  }

  public List<WAComponent> getVisibleComponents(OrganizationController orgaController) {
    List<WAComponent> visibleComponents = new ArrayList<WAComponent>();
    Collection<WAComponent> components = orgaController.getAllComponents().values();
    for (WAComponent component : components) {
      if (component.isVisibleInPersonalSpace()) {
        visibleComponents.add(component);
      }
    }
    return visibleComponents;
  }

  public String addComponent(String userId, String componentName, String componentLabel)
      throws AdminException, QuotaException {
    ComponentInst component = new ComponentInst();
    component.setCreatorUserId(userId);
    component.setInheritanceBlocked(false);
    component.setLabel(componentLabel);
    component.setName(componentName);

    Optional<WAComponent> wac = WAComponent.get(componentName);
    if (!wac.isPresent() || !wac.get().isVisibleInPersonalSpace()) {
      UserDetail user = UserDetail.getById(userId);
      LocalizationBundle messages = getMessages(user.getUserPreferences().getLanguage());
      String errorText = messages.getString("JSPP.ErrorUnknownComponent");
      throw new AdminException(MessageFormat.format(errorText, componentName), false);
    }
    List<Parameter> parameters = wac.get().getAllParameters();

    // set specific parameter values for personal space context
    for (Parameter parameter : parameters) {
      if (StringUtil.isDefined(parameter.getPersonalSpaceValue())) {
        parameter.setValue(parameter.getPersonalSpaceValue());
      }
    }
    component.setParameters(parameters);

    SpaceInst space = getPersonalSpace(userId);

    if (space == null) {
      // if user has no personal space, creates one
      space = new SpaceInst();
      space.setCreatorUserId(userId);
      // space.setDomainFatherId("0");
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
      space.addComponentInst(component);
      getAdminService().addSpaceInst(userId, space);

    } else {
      // if user has his personal space, just add component in it
      component.setDomainFatherId(space.getId());
      getAdminService().addComponentInst(userId, component);
    }
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
      SilverLogger.getLogger("admin").warn(e.getMessage());
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
