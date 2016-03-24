/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.core.webapi.base.aspect;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.silverpeas.core.util.StringUtil.isDefined;

import org.silverpeas.core.webapi.base.WebResource;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.silverpeas.core.admin.service.OrganizationController;

/**
 * An aspect to insert component existence checking in the web services so that it is performed
 * implicitly for each web resources managed by a given component instance. If a web resource
 * doesn't belong to any component instance, then nothing is done.
 */
@Interceptor
@ComponentInstMustExistIfSpecified
@Priority(APPLICATION)
public class ComponentExistenceAspect {

  @AroundInvoke
  public Object processAuthorization(InvocationContext context) throws Exception {
    Object target = context.getTarget();
    if (target instanceof WebResource) {
      WebResource resource = (WebResource) target;
      String instanceId = null;
      try {
        instanceId = resource.getComponentId();
      } catch (Exception ignore) {
      }
      if (isDefined(instanceId)) {
        OrganizationController controller =
            OrganizationControllerProvider.getOrganisationController();
        if (!controller.isComponentExist(instanceId) && !controller.isToolAvailable(instanceId) &&
            !controller.isAdminTool(instanceId)) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      }
    }
    return context.proceed();
  }
}