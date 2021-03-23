/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.annotation;

import org.apache.commons.lang3.NotImplementedException;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.model.PasteDetailFromToPK;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.util.MapUtil;

import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 22/10/13
 */
public class AnnotationUtil {

  private AnnotationUtil() {

  }

  /**
   * Provides a centralized way to extract annotation of a method.
   * @param method the intercepted method that will be invoked.
   * @return the map with keys of Annotation class and values of object list.
   */
  private static Map<Class<? extends Annotation>, Annotation> extractMethodAnnotations(
      Method method) {

    // Initializing the results
    Map<Class<? extends Annotation>, Annotation> results = new LinkedHashMap<>();

    for (Annotation annotation : method.getAnnotations()) {
      results.put(annotation.annotationType(), annotation);
    }

    return results;
  }

  /**
   * Provides a centralized way to extract annotated method parameter values.
   * @param method the intercepted method that will be invoked.
   * @return the map with keys of Annotation class and values of object list.
   */
  private static Map<Class<? extends Annotation>, List<Object>> extractMethodAnnotatedParameterValues(
      Method method, Object[] parameterValues) {

    // Initializing the results
    Map<Class<? extends Annotation>, List<Object>> results = new LinkedHashMap<>();

    Annotation[][] annotations = method.getParameterAnnotations();
    for (int i = 0; i < annotations.length; i++) {
      Annotation[] parameterAnnotations = annotations[i];
      for (Annotation parameterAnnotation : parameterAnnotations) {
        Object parameterValue = parameterValues[i];
        if (parameterValue == null) {
          continue;
        }
        processAnnotations(results, parameterAnnotation, parameterValue);
      }
    }

    return results;
  }

  private static void processAnnotations(final Map<Class<? extends Annotation>, List<Object>> results,
      final Annotation parameterAnnotation, final Object parameterValue) {
    if (parameterAnnotation.annotationType().isAssignableFrom(SourcePK.class) ||
        parameterAnnotation.annotationType().isAssignableFrom(TargetPK.class)) {

      if (parameterValue instanceof Collection) {
        for (Object value : ((Collection<?>) parameterValue)) {
          addPKParameterValue(results, parameterAnnotation.annotationType(), value);
        }
      } else {
        addPKParameterValue(results, parameterAnnotation.annotationType(), parameterValue);
      }

    } else if (parameterAnnotation.annotationType().isAssignableFrom(Language.class)) {

      String language = null;
      if (parameterValue instanceof String) {
        language = (String) parameterValue;
      } else if (parameterValue instanceof Locale) {
        language = ((Locale) parameterValue).getLanguage();
      }
      MapUtil.putAddList(results, parameterAnnotation.annotationType(), language);

    } else {
      MapUtil.putAddList(results, parameterAnnotation.annotationType(), parameterValue);
    }
  }

  /**
   * This method extracts from an object parameter annotated by {@link SourcePK} or {@link TargetPK}
   * the instance of {@link ResourceReference} that it contains. The extracted functional key is
   * added to the given container parameterValue. If the type of the annotated object is not
   * handled, an exception is thrown.
   * @param parameterValues the container of functional key values of parameters.
   * @param annotationClass the class of the annotation that indicate to take in account a method
   * parameter.
   * @param object the instance of the parameter.
   * @throws NotImplementedException when trying to extract a {@link ResourceReference} from an
   * object that is not yet handled. If the object you try to handle is a centralized one, that is
   * to say, used by several components, then you can add the support of this object by this method.
   * If not, try to implement {@link SilverpeasContent} interface.
   */
  private static void addPKParameterValue(
      Map<Class<? extends Annotation>, List<Object>> parameterValues,
      Class<? extends Annotation> annotationClass, Object object) {
    ResourceReference resourceRef;
    if (object instanceof Contribution) {
      resourceRef = ((Contribution) object).getIdentifier().toReference();
    } else if (object instanceof PasteDetailFromToPK) {
      PasteDetailFromToPK<?, ?> pasteDetail = (PasteDetailFromToPK<?, ?>) object;
      if (SourcePK.class.equals(annotationClass)) {
        resourceRef = pasteDetail.getFromPK();
      } else {
        resourceRef = pasteDetail.getToPK();
      }
    } else if (object instanceof ResourceReference) {
      resourceRef = (ResourceReference) object;
    } else {
      resourceRef = null;
      if (object != null) {
        throw new NotImplementedException(
            "No implementation to address type " + object.getClass().getName() +
                " in simulation processing of actions");
      }
    }

    if (resourceRef != null) {
      MapUtil.putAddList(parameterValues, annotationClass, resourceRef);
    }
  }

  /**
   * Provides a centralized way to extract annotation of a method.
   * @param invocationContext the context of invocation.
   * @return the map with keys of Annotation class and values of object list.
   */
  public static Map<Class<? extends Annotation>, Annotation> extractMethodAnnotations(
      InvocationContext invocationContext) {
    return extractMethodAnnotations(getInterceptedMethodFromContext(invocationContext));
  }

  /**
   * Provides a centralized way to extract annotated method parameter values.
   * @param invocationContext the context of invocation.
   * @return the map with keys of Annotation class and values of object list.
   */
  public static Map<Class<? extends Annotation>, List<Object>> extractMethodAnnotatedParameterValues(
      InvocationContext invocationContext) {
    return extractMethodAnnotatedParameterValues(getInterceptedMethodFromContext(invocationContext),
        invocationContext.getParameters());
  }

  /**
   * Gets the list of annotation instances from the given list of annotations which the type is
   * the one given.
   * @param annotatedValues container that contains several types of annotation instances.
   * @return List empty or not (but never null)
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> getAnnotatedValues(
      Map<Class<? extends Annotation>, List<Object>> annotatedValues,
      Class<? extends Annotation> annotationClass) {
    List<T> result = null;

    if (annotatedValues != null) {
      result = (List<T>) annotatedValues.get(annotationClass);
    }

    if (result == null) {
      result = new ArrayList<>(0);
    }
    return result;
  }

  /**
   * Provides a centralized way to find the method that has been intercepted.
   * @param invocationContext the context of invocation.
   * @return the method that has been intercepted.
   */
  private static Method getInterceptedMethodFromContext(InvocationContext invocationContext) {
    return invocationContext.getMethod();
  }
}
