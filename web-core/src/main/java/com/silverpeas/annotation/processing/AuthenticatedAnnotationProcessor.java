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
package com.silverpeas.annotation.processing;

import com.silverpeas.web.RESTWebService;
import com.silverpeas.web.UserPriviledgeValidation;
import com.silverpeas.web.UserPriviledgeValidationFactory;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * A processor working on the classes that are annoted with the @Authenticated annotation. As the
 * annoted classes requires to be modified by adding to them some codes, the processor is actually
 * an aspect.
 */
@Component @Aspect
public class AuthenticatedAnnotationProcessor {

  @Pointcut(
  "@within(com.silverpeas.annotation.Authenticated) && this(com.silverpeas.web.RESTWebService)")
  public void webServicesAnnotatedWithAuthenticated() {
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

  @Before("webServicesAnnotatedWithAuthenticated() && (methodAnnotatedWithGET() || "
  + "methodAnnotatedWithPOST() || methodAnnotatedWithDELETE() || methodAnnotatedWithPUT()) "
  + "&& this(service)")
  public void validateTheAuthentication(RESTWebService service) throws Throwable {
    UserPriviledgeValidation validation = UserPriviledgeValidationFactory.getFactory().
            getUserPriviledgeValidation();
    service.validateUserAuthentication(validation);
  }

}
