/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.process.annotation;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.annotation.Action;
import org.silverpeas.core.util.annotation.AnnotationUtil;
import org.silverpeas.core.util.annotation.Language;
import org.silverpeas.core.util.annotation.SourceObject;
import org.silverpeas.core.util.annotation.SourcePK;
import org.silverpeas.core.util.annotation.TargetPK;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.annotation.AnnotationUtil.getAnnotatedValues;

/**
 * Interceptor that handle a simulation of actions performed by a caller.
 * More precisely, the simulation focuses on file manipulations.
 * All method called annotated with {@link SimulationActionProcess} and managed by CDI,
 * will be processed by this interceptor.
 * @author Yohann Chastagnier
 */
@InterceptorBindingOfSimulationActionProcess
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class SimulationActionProcessAnnotationInterceptor {

  @AroundInvoke
  public Object intercept(InvocationContext invocationContext) {

    // Retrieving the method annotations
    Map<Class<? extends Annotation>, Annotation> methodAnnotations =
        AnnotationUtil.extractMethodAnnotations(invocationContext);

    // Retrieving source PKs and target objects and/or PKs
    Map<Class<? extends Annotation>, List<Object>> annotatedParametersValues =
        AnnotationUtil.extractMethodAnnotatedParameterValues(invocationContext);

    // Processing
    return perform(invocationContext, methodAnnotations, annotatedParametersValues);
  }

  protected Object perform(InvocationContext context,
      Map<Class<? extends Annotation>, Annotation> methodAnnotations,
      Map<Class<? extends Annotation>, List<Object>> annotatedParametersValues) {
    return SimulationActionProcessProcessor.get()
      .withContext(s -> {
            final List<ResourceReference> sourcePKs = getAnnotatedValues(annotatedParametersValues, SourcePK.class);
            s.getSourcePKs().addAll(sourcePKs);
            final List<Object> sourceObjects = getAnnotatedValues(annotatedParametersValues, SourceObject.class);
            s.getSourceObjects().addAll(sourceObjects);
          })
          .listElementsWith(() -> {
            final SimulationActionProcess simulationActionProcess = getSimulationActionProcess(methodAnnotations);
            try {
              final Constructor<? extends SimulationElementLister> constructor = simulationActionProcess.elementLister().getDeclaredConstructor();
              return constructor.newInstance();
            } catch (Exception e) {
              throw new SilverpeasRuntimeException(e);
            }
          })
          .byAction(() -> {
            final Action actionType = getAction(methodAnnotations);
            return actionType.value();
          })
      .toTargets(t -> {
        final List<ResourceReference> targetPKs = getAnnotatedValues(annotatedParametersValues, TargetPK.class);
        t.getTargetPKs().addAll(targetPKs);
      })
      .setLanguage(() -> {
        final List<Object> languages = getAnnotatedValues(annotatedParametersValues, Language.class);
        return languages.isEmpty() ? null : (String) languages.get(0);
      })
      .fromMethod(context.getMethod())
      .execute(() -> proceed(context));
  }

  @Nonnull
  private Action getAction(final Map<Class<? extends Annotation>, Annotation> methodAnnotations) {
    Action actionType = (Action) methodAnnotations.get(Action.class);

    // Technical assertion
    if (actionType == null) {
      throw new AssertionError(
          "actionType is null (Action annotation must be specified on the method)");
    }
    return actionType;
  }

  @Nonnull
  private SimulationActionProcess getSimulationActionProcess(
      final Map<Class<? extends Annotation>, Annotation> methodAnnotations) {
    SimulationActionProcess simulationActionProcess =
        (SimulationActionProcess) methodAnnotations.get(SimulationActionProcess.class);

    // Technical assertion
    if (simulationActionProcess == null) {
      // This verification is not a stupid one. Indeed, the interceptor can be specified
      // directly via EJB @Interceptors annotation and it can be forgotten to specify the
      // required {@link SimulationActionProcess} annotation.
      throw new AssertionError(
          "simulationActionProcess is null (SimulationActionProcess annotation must" +
              " be specified on the method)");
    }
    return simulationActionProcess;
  }

  protected Object proceed(final InvocationContext context) throws Exception {
    return context.proceed();
  }
}
