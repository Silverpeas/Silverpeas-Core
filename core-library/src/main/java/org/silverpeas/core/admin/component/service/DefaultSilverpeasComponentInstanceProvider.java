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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.admin.component.service;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasPersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasSharedComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class DefaultSilverpeasComponentInstanceProvider
    implements SilverpeasComponentInstanceProvider {

  @Override
  public Optional<SilverpeasComponentInstance> getById(final String componentInstanceId) {
    return OrganizationController.get().getComponentInstance(componentInstanceId);
  }

  @Override
  public Optional<SilverpeasSharedComponentInstance> getSharedById(
      final String sharedComponentInstanceId) {
    SilverpeasComponentInstance instance = getById(sharedComponentInstanceId).orElse(null);
    if (instance instanceof SilverpeasSharedComponentInstance) {
      return Optional.of((SilverpeasSharedComponentInstance) instance);
    }
    return Optional.empty();
  }

  @Override
  public Optional<SilverpeasPersonalComponentInstance> getPersonalById(
      final String personalComponentInstanceId) {
    return Optional
        .ofNullable(PersonalComponentInstance.from(personalComponentInstanceId).orElse(null));
  }

  @Override
  public String getComponentName(final String componentInstanceId) {
    String componentName = ComponentInst.getComponentName(componentInstanceId);
    if (StringUtil.isNotDefined(componentName)) {
      componentName = PersonalComponentInstance.getComponentName(componentInstanceId);
    }
    return componentName;
  }
}
