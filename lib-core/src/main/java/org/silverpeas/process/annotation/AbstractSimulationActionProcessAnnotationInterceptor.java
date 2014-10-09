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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.ActionType;
import org.silverpeas.util.WAPrimaryKey;
import org.silverpeas.util.annotation.Action;
import org.silverpeas.util.annotation.AnnotationUtil;
import org.silverpeas.util.annotation.Language;
import org.silverpeas.util.annotation.SourcePK;
import org.silverpeas.util.annotation.TargetObject;
import org.silverpeas.util.annotation.TargetPK;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.process.ProcessFactory;
import org.silverpeas.process.management.ProcessExecutionContext;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstraction of interceptors that handle a simulation of actions performed by a caller.
 * More precisely, the simulation focuses on file manipulations.
 * In the future this kind of interceptor will be transformed in a CDI one.
 * That is why, callers have to specify it with a second one :
 * {@link SimulationActionProcess}
 * After CDI transformation, all Interceptors(Class) annotation will be removed.
 */
public abstract class AbstractSimulationActionProcessAnnotationInterceptor<C> {

  protected static final String SIMULATION_PROCESS_PERFORMED = "SIMULATION_PROCESS_PERFORMED";

  @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
  protected Object perform(C context, Map<Class<Annotation>, Annotation> methodAnnotations,
      Map<Class<Annotation>, List<Object>> annotedParametersValues) throws Exception {

    // Simulation is processed only if no simulation is already working
    if (CacheServiceFactory.getRequestCacheService().get(SIMULATION_PROCESS_PERFORMED) == null) {
      try {

        // Master annotation
        SimulationActionProcess simulationActionProcess =
            (SimulationActionProcess) methodAnnotations.get(SimulationActionProcess.class);

        // Technical assertion
        if (simulationActionProcess == null) {
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
            AnnotationUtil.getAnnotedValues(annotedParametersValues, Language.class);
        String language = languages.isEmpty() ? null : (String) languages.get(0);
        List<WAPrimaryKey> sourcePKs =
            (List) AnnotationUtil.getAnnotedValues(annotedParametersValues, SourcePK.class);
        List<WAPrimaryKey> targetPKs =
            (List) AnnotationUtil.getAnnotedValues(annotedParametersValues, TargetPK.class);
        List<Object> targetObjects =
            (List) AnnotationUtil.getAnnotedValues(annotedParametersValues, TargetObject.class);

        // Performing verify if necessary
        if ((!sourcePKs.isEmpty() || !targetObjects.isEmpty()) && !targetPKs.isEmpty()) {

          // Element container
          Map<Class<SimulationElement>, List<SimulationElement>> elements =
              new HashMap<Class<SimulationElement>, List<SimulationElement>>();

          // Processing for each target
          for (WAPrimaryKey targetPK : targetPKs) {

            // Element lister
            SimulationElementLister elementLister =
                simulationActionProcess.elementLister().newInstance();
            elementLister.setActionType(actionType.value());
            elementLister.setElements(elements);

            // Browsing sourcePKs
            for (WAPrimaryKey sourcePK : sourcePKs) {
              if (ActionType.MOVE != actionType.value()
                  || !sourcePK.getInstanceId().equals(targetPK.getInstanceId())) {
                elementLister.listElements(sourcePK, language);
              }
            }

            // Browsing sourceObjects
            for (Object targetObject : targetObjects) {
              elementLister.listElements(targetObject, language, targetPK);
            }
            
            ProcessFactory.getProcessManagement().execute(
                new SimulationElementConversionProcess(elements, targetPK, actionType.value()),
                new ProcessExecutionContext(targetPK.getInstanceId()));
          }

          // Indicating that a functional check has been performed
          CacheServiceFactory.getRequestCacheService().put(SIMULATION_PROCESS_PERFORMED, true);
        }

        // Invoking finally the proxy method initially called
        return proceed(context);

      } catch (Exception e) {
        SilverTrace.error("Process", "SimulationActionProcessAnnotationEJBInterceptor.process()",
            "process.EXCEPTION_FROM_ACTION_PROCESS_SERVICE", e);
        throw e;
      } finally {

        // Removing the flag indicating that a functional check has been performed (out of the
        // service)
        CacheServiceFactory.getRequestCacheService().remove(SIMULATION_PROCESS_PERFORMED);
      }
    }

    // Invoking finally the proxy method initially called
    return proceed(context);
  }

  protected abstract Object proceed(C context) throws Exception;
}
