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
package org.silverpeas.core.process.annotation;

import org.silverpeas.core.process.ProcessProvider;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.util.annotation.Action;
import org.silverpeas.core.util.annotation.AnnotationUtil;
import org.silverpeas.core.util.annotation.Language;
import org.silverpeas.core.util.annotation.SourceObject;
import org.silverpeas.core.util.annotation.SourcePK;
import org.silverpeas.core.util.annotation.TargetPK;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  private static final String SIMULATION_PROCESS_PERFORMED = "@SIMULATION_PROCESS_PERFORMED@";

  @AroundInvoke
  public Object intercept(InvocationContext invocationContext) throws Exception {

    // Retrieving the method annotations
    Map<Class<Annotation>, Annotation> methodAnnotations =
        AnnotationUtil.extractMethodAnnotations(invocationContext);

    // Retrieving source PKs and target objects and/or PKs
    Map<Class<Annotation>, List<Object>> annotatedParametersValues =
        AnnotationUtil.extractMethodAnnotatedParameterValues(invocationContext);

    // Processing
    return perform(invocationContext, methodAnnotations, annotatedParametersValues);
  }

  @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
  protected Object perform(InvocationContext context,
      Map<Class<Annotation>, Annotation> methodAnnotations,
      Map<Class<Annotation>, List<Object>> annotatedParametersValues) throws Exception {

    // Simulation is processed only if no simulation is already working
    if (CacheServiceProvider.getRequestCacheService().get(SIMULATION_PROCESS_PERFORMED) == null) {
      try {

        // Master annotation
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

        // Action
        Action actionType = (Action) methodAnnotations.get(Action.class);

        // Technical assertion
        if (actionType == null) {
          throw new AssertionError(
              "actionType is null (Action annotation must be specified on the method)");
        }

        // Retrieving aimed parameter values
        List<Object> languages =
            AnnotationUtil.getAnnotatedValues(annotatedParametersValues, Language.class);
        String language = languages.isEmpty() ? null : (String) languages.get(0);
        List<WAPrimaryKey> sourcePKs =
            (List) AnnotationUtil.getAnnotatedValues(annotatedParametersValues, SourcePK.class);
        List<WAPrimaryKey> targetPKs =
            (List) AnnotationUtil.getAnnotatedValues(annotatedParametersValues, TargetPK.class);
        List<Object> sourceObjects =
            (List) AnnotationUtil.getAnnotatedValues(annotatedParametersValues, SourceObject.class);

        // Performing verify if necessary
        if ((!sourcePKs.isEmpty() || !sourceObjects.isEmpty()) && !targetPKs.isEmpty()) {

          // Element container
          Map<Class<SimulationElement>, List<SimulationElement>> elements = new HashMap<>();

          // Processing for each target
          for (WAPrimaryKey targetPK : targetPKs) {

            // Element lister
            SimulationElementLister elementLister =
                simulationActionProcess.elementLister().newInstance();
            elementLister.setActionType(actionType.value());
            elementLister.setElements(elements);

            // Browsing sourcePKs
            sourcePKs.stream().filter(sourcePK -> ActionType.MOVE != actionType.value() ||
                !sourcePK.getInstanceId().equals(targetPK.getInstanceId()))
                .forEach(sourcePK -> elementLister.listElements(sourcePK, language));

            // Browsing sourceObjects
            for (Object sourceObject : sourceObjects) {
              elementLister.listElements(sourceObject, language, targetPK);
            }

            ProcessProvider.getProcessManagement().execute(
                new SimulationElementConversionProcess(elements, targetPK, actionType.value()),
                new ProcessExecutionContext(targetPK.getInstanceId()));
          }

          // Indicating that a functional check has been performed
          CacheServiceProvider.getRequestCacheService().put(SIMULATION_PROCESS_PERFORMED, true);
        } else {
          SilverTrace.warn("Process", "SimulationActionProcessAnnotationInterceptor.intercept()",
              "intercepted method '" + context.getMethod().getName() +
                  "', but SourcePK, SourceObject or TargetPK annotations are missing on parameter" +
                  " specifications...");
        }

        // Invoking finally the proxy method initially called
        return proceed(context);

      } catch (Exception e) {
        SilverTrace.error("Process", "SimulationActionProcessAnnotationInterceptor.intercept()",
            "process.EXCEPTION_FROM_ACTION_PROCESS_SERVICE", e);
        throw e;
      } finally {

        // Removing the flag indicating that a functional check has been performed (out of the
        // service)
        CacheServiceProvider.getRequestCacheService().remove(SIMULATION_PROCESS_PERFORMED);
      }
    }

    // Invoking finally the proxy method initially called
    return proceed(context);
  }

  protected Object proceed(final InvocationContext context) throws Exception {
    return context.proceed();
  }
}
