/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.web.aspect;

import static com.silverpeas.util.StringUtil.isDefined;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * An aspect to insert component existence checking in the web services so that it is performed
 * implicitly for each web resources managed by a given component instance. If a web resource
 * doesn't belong to any component instance, then nothing is done.
 */
@Component @Aspect
public class ComponentExistenceAspect {

  @Pointcut("@within(javax.ws.rs.Path) && this(com.silverpeas.web.RESTWebService)")
  public void webServices() {
  }

  @Pointcut("@annotation(javax.ws.rs.GET) && execution(* *(..))")
  public void methodAnnotatedWithGET() {
  }

  @Pointcut("@annotation(javax.ws.rs.POST) && execution(* *(..))")
  public void methodAnnotatedWithPOST() {
  }

  @Pointcut("@annotation(javax.ws.rs.DELETE) && execution(* *(..))")
  public void methodAnnotatedWithDELETE() {
  }

  @Pointcut("@annotation(javax.ws.rs.PUT) && execution(* *(..))")
  public void methodAnnotatedWithPUT() {
  }

  @Before("webServices() && (methodAnnotatedWithGET() || "
  + "methodAnnotatedWithPOST() || methodAnnotatedWithDELETE() || methodAnnotatedWithPUT()) "
  + "&& this(service)")
  public void checkComponentInstanceExistence(RESTWebService service) throws Throwable {
    String instanceId = null;
    try {
      instanceId = service.getComponentId();
    } catch (Exception ex) {
    }
    if (isDefined(instanceId)) {
      OrganizationController controller = OrganizationControllerFactory.getFactory().
              getOrganizationController();
      if (!controller.isComponentExist(instanceId) && !controller.isToolAvailable(instanceId)) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }
  }
}
