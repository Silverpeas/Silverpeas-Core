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

package org.silverpeas.cmis.security;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.SpaceAccessControl;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A register of access controllers used by the CMIS services to check the authorization of the
 * users to access a Silverpeas resource through the CMIS repository.
 * @author mmoquillon
 */
@Bean
@Singleton
public class AccessControllerRegister {

  private static final AccessController<String> DEFAULT_ACCESS_CTRL = new GrantedAccessController();

  private final Map<String, Supplier<AccessController<String>>> controllers = new HashMap<>();

  @PostConstruct
  private void init() {
    controllers.put(SpaceInstLight.class.getSimpleName(), SpaceAccessControl::get);
    controllers.put(ComponentInstLight.class.getSimpleName(), ComponentAccessControl::get);
  }

  /**
   * Gets the access controller that works on the specified type of resources in Silverpeas. If no
   * access controller is found for the specified type of resources, then a
   * {@link GrantedAccessController} instance is returned and then the access is granted to
   * everyone that is authenticated in Silverpeas.
   * @param objectClass the type of a resource in Silverpeas.
   * @return an {@link AccessController} object.
   */
  public AccessController<String> getAccessController(final Class<?> objectClass) {
    final Supplier<AccessController<String>> supplier =
        controllers.getOrDefault(objectClass.getSimpleName(), () -> DEFAULT_ACCESS_CTRL);
    return supplier.get();
  }
}
  