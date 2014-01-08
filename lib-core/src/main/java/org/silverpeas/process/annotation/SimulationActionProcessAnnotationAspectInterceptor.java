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

import com.stratelia.webactiv.util.annotation.AnnotationUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
@Component
@Aspect
public class SimulationActionProcessAnnotationAspectInterceptor
    extends AbstractSimulationActionProcessAnnotationInterceptor<ProceedingJoinPoint> {

  @Pointcut(
      "@annotation(org.silverpeas.process.annotation.SimulationActionProcess) && " +
          "execution(* *(..))")
  public void methodAnnotatedWithSimulationActionProcess() {
  }

  @Around("methodAnnotatedWithSimulationActionProcess()")
  public Object perform(ProceedingJoinPoint invocationContext) throws Throwable {

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
  protected Object proceed(final ProceedingJoinPoint context) throws Exception {
    try {
      return context.proceed();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw e;
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }
}
