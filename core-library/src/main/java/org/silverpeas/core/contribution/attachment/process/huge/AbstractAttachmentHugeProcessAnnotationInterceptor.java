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
package org.silverpeas.core.contribution.attachment.process.huge;

import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.annotation.AnnotationUtil;
import org.silverpeas.core.util.annotation.SourceObject;
import org.silverpeas.core.util.annotation.SourcePK;
import org.silverpeas.core.util.annotation.TargetPK;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interceptor abstraction to centralize common code about management of following annotations:
 * <ul>
 *   <li>{@link AttachmentHugeProcess}</li>
 *   <li>{@link PreventAttachmentHugeProcess}</li>
 * </ul>
 * @author silveryocha
 * @see AttachmentHugeProcess
 * @see PreventAttachmentHugeProcess
 */
public abstract class AbstractAttachmentHugeProcessAnnotationInterceptor {

  @AroundInvoke
  public Object intercept(InvocationContext invocationContext) throws Exception {
    // Retrieving source PKs and target objects and/or PKs
    Map<Class<? extends Annotation>, List<Object>> annotatedParametersValues =
        AnnotationUtil.extractMethodAnnotatedParameterValues(
        invocationContext);
    // Processing
    return perform(invocationContext, annotatedParametersValues);
  }

  @SuppressWarnings("deprecation")
  protected Object perform(InvocationContext context,
      Map<Class<? extends Annotation>, List<Object>> annotatedParametersValues) throws Exception {
    Set<String> instanceIds = null;
    try {
      // Retrieving aimed parameter values
      final List<WAPrimaryKey> sourcePKs = AnnotationUtil.getAnnotatedValues(
          annotatedParametersValues, SourcePK.class);
      final List<WAPrimaryKey> targetPKs = AnnotationUtil.getAnnotatedValues(
          annotatedParametersValues, TargetPK.class);
      final List<Object> sourceObjects = AnnotationUtil.getAnnotatedValues(
          annotatedParametersValues, SourceObject.class);
      // Performing verify if necessary
      if (!sourcePKs.isEmpty() || !sourceObjects.isEmpty() || !targetPKs.isEmpty()) {
        // Get all instance ids involved
        instanceIds = Stream.concat(
                Stream.concat(sourcePKs.stream().map(WAPrimaryKey::getInstanceId),
                              targetPKs.stream().map(WAPrimaryKey::getInstanceId)),
                sourceObjects.stream()
                    .map(o -> {
                      if (o instanceof SimpleDocument) {
                        return ((SimpleDocument) o).getInstanceId();
                      }
                      return o.toString();
                    }))
            .collect(Collectors.toSet());
        startForInstances(instanceIds);
      } else {
        SilverLogger.getLogger(this)
            .warn(
                "Intercepted method ''{0}'', but SourcePK, SourceObject or TargetPK annotations" +
                    "  are missing on parameter specifications...", context.getMethod().getName());
      }
      // Invoking finally the proxy method initially called
      return proceed(context);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Error in intercepted method: " + context.getMethod().getName(), e);
      throw e;
    } finally {
      if (instanceIds != null) {
        endForInstances(instanceIds);
      }
    }
  }

  protected abstract void startForInstances(final Set<String> instanceIds);
  protected abstract void endForInstances(final Set<String> instanceIds);

  protected Object proceed(final InvocationContext context) throws Exception {
    return context.proceed();
  }
}
