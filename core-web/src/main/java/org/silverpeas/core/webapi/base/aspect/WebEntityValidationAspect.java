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

import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.util.annotation.AnnotationUtil;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * An aspect to insert component existence checking in the web services so that it is performed
 * implicitly for each web resources managed by a given component instance. If a web resource
 * doesn't belong to any component instance, then nothing is done.
 */
@Interceptor
@WebEntityMustBeValid
@Priority(APPLICATION)
public class WebEntityValidationAspect {

  @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
  @AroundInvoke
  public <A extends Annotation, T extends WebEntity> Object processAuthorization(
      InvocationContext context) throws Exception {
    Map<Class<A>, A> methodAnnotations = AnnotationUtil.extractMethodAnnotations(context);
    if (methodAnnotations.containsKey(POST.class) || methodAnnotations.containsKey(PUT.class)) {
      for (Object parameterValue : context.getParameters()) {
        if (parameterValue instanceof WebEntity) {
          T webEntity = (T) parameterValue;
          ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
          Validator validator = factory.getValidator();
          Set<ConstraintViolation<T>> violations = validator.validate(webEntity);
          if (!violations.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
          }
        }
      }
    }
    return context.proceed();
  }
}
