/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.stub;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasPersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasSharedComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.annotation.Provider;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Component instances provider implementation to be used in integration tests without having to
 * link to the concrete implementation of the interface in Silverpeas Core Library.
 *
 * @author mmoquillon
 */
@Provider
public class StubbedComponentInstanceProvider implements SilverpeasComponentInstanceProvider {

  private static final Set<SilverpeasComponentInstance> instances = new HashSet<>();

  public static void addComponentInstance(SilverpeasComponentInstance componentInst) {
    instances.add(componentInst);
  }

  @Override
  public Optional<SilverpeasComponentInstance> getById(String componentInstanceId) {
    return instances.stream()
        .filter(c -> c.getId().equals(componentInstanceId))
        .findFirst();
  }

  @Override
  public Optional<SilverpeasSharedComponentInstance> getSharedById(String sharedComponentInstanceId) {
    return Optional.empty();
  }

  @Override
  public Optional<SilverpeasPersonalComponentInstance> getPersonalById(String personalComponentInstanceId) {
    return Optional.empty();
  }
}
  