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

import org.silverpeas.core.web.rs.WebEntity;
import org.silverpeas.core.util.annotation.AnnotationUtil;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

/**
 * An aspect to insert component existence checking in the web services so that it is performed
 * implicitly for each web resources managed by a given component instance. If a web resource
 * doesn't belong to any component instance, then nothing is done.
 */
@Interceptor
@WebEntityMustBeValid
@Priority(APPLICATION)
public class WebEntityValidationAspect {

  @SuppressWarnings("unchecked")
  @AroundInvoke
  public <T extends WebEntity> Object processAuthorization(
      InvocationContext context) throws Exception {
    Map<Class<? extends Annotation>, Annotation> methodAnnotations =
        AnnotationUtil.extractMethodAnnotations(context);
    if (methodAnnotations.containsKey(POST.class) || methodAnnotations.containsKey(PUT.class)) {
      for (Object parameterValue : context.getParameters()) {
        if (parameterValue instanceof WebEntity) {
          T webEntity = (T) parameterValue;
          try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<T>> violations = validator.validate(webEntity);
            if (!violations.isEmpty()) {
              throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
          }
        }
      }
    }
    return context.proceed();
  }
}
