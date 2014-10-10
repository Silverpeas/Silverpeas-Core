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
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.admin.components.Instanciateur;
import com.silverpeas.admin.components.Parameter;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.quota.exception.QuotaException;

import static com.stratelia.webactiv.beans.admin.AdminReference.getAdminService;

public class PersonalSpaceController {

  private static final String MESSAGES_LOCATION =
      "org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle";

  public List<WAComponent> getVisibleComponents(OrganisationController orgaController) {
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

    WAComponent baseComponent = Instanciateur.getWAComponent(componentName);
    if (baseComponent == null || !baseComponent.isVisibleInPersonalSpace()) {
      UserDetail user = UserDetail.getById(userId);
      ResourceLocator messages = getMessages(user.getUserPreferences().getLanguage());
      String errorText = messages.getString("JSPP.ErrorUnknownComponent");
      throw new AdminException(MessageFormat.format(errorText, componentName), false);
    }
    List<Parameter> parameters = baseComponent.getParameters();

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
    return componentName + component.getId();
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
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      return null;
    }
  }

  private ComponentInst getComponent(SpaceInst space, String componentId) {
    List<ComponentInst> components = space.getAllComponentsInst();
    for (ComponentInst component : components) {
      if ((component.getName() + component.getId()).equalsIgnoreCase(componentId)) {
        return component;
      }
    }
    return null;
  }

  private ResourceLocator getMessages(String language) {
    return new ResourceLocator(MESSAGES_LOCATION, language);
  }
}
