/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.process.annotation;

import org.silverpeas.util.annotation.AnnotationUtil;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * The interceptor to indicate on EJB method services while CDI is not fully used.
 * @author Yohann Chastagnier
 */
public class SimulationActionProcessAnnotationEJBInterceptor
    extends AbstractSimulationActionProcessAnnotationInterceptor<InvocationContext> {

  @AroundInvoke
  public Object process(InvocationContext invocationContext) throws Exception {

    // Retrieving the method annotations
    Map<Class<Annotation>, Annotation> methodAnnotations =
        AnnotationUtil.extractMethodAnnotations(invocationContext);

    // Retrieving source PKs and target objects and/or PKs
    Map<Class<Annotation>, List<Object>> annotedParametersValues =
        AnnotationUtil.extractMethodAnnotedParameterValues(invocationContext);

    // Processing
    return perform(invocationContext, methodAnnotations, annotedParametersValues);
  }

  @Override
  protected Object proceed(final InvocationContext context) throws Exception {
    return context.proceed();
  }
}
