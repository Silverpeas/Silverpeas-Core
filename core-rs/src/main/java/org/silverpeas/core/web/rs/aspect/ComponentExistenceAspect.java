/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.rs.aspect;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.web.rs.ProtectedWebResource;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * An aspect to insert component existence checking in the web services so that it is performed
 * implicitly for each web resources managed by a given component instance. If a web resource
 * doesn't belong to any component instance, then nothing is done.
 */
@Interceptor
@ComponentInstMustExistIfSpecified
@Priority(APPLICATION)
public class ComponentExistenceAspect {

  @Inject
  private OrganizationController controller;

  @AroundInvoke
  public Object processAuthorization(InvocationContext context) throws Exception {
    Object target = context.getTarget();
    if (target instanceof ProtectedWebResource) {
      ProtectedWebResource resource = (ProtectedWebResource) target;
      String instanceId = null;
      try {
        instanceId = resource.getComponentId();
      } catch (Exception ignore) {
        // ignore the exception
      }
      if (isDefined(instanceId) && PersonalComponentInstance.from(instanceId).isEmpty() &&
            !controller.isComponentExist(instanceId) &&
            !controller.isToolAvailable(instanceId) &&
            !controller.isAdminTool(instanceId)) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }
    return context.proceed();
  }
}