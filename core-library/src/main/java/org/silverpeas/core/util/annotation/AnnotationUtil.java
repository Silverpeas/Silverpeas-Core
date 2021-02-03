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
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.component.model.PasteDetailFromToPK;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
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

  /**
   * Provides a centralized way to extract annotation of a method.
   * @param invocationContext the context of invocation.
   * @return the map with keys of Annotation class and values of object list.
   * @throws Exception
   */
  public static <A extends Annotation> Map<Class<A>, A> extractMethodAnnotations(
      InvocationContext invocationContext) throws Exception {
    return extractMethodAnnotations(getInterceptedMethodFromContext(invocationContext));
  }

  /**
   * Provides a centralized way to extract annotation of a method.
   * @param method the intercepted method that will be invoked.
   * @param <A> the type of an annotation.
   * @return the map with keys of Annotation class and values of object list.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private static <A extends Annotation> Map<Class<A>, A> extractMethodAnnotations(Method method)
      throws Exception {

    // Initializing the results
    Map<Class<? extends Annotation>, Annotation> results = new LinkedHashMap<>();

    for (Annotation annotation : method.getAnnotations()) {
      results.put(annotation.annotationType(), annotation);
    }

    return (Map) results;
  }

  /**
   * Provides a centralized way to extract annotated method parameter values.
   * @param invocationContext the context of invocation.
   * @return the map with keys of Annotation class and values of object list.
   * @throws Exception
   */
  public static Map<Class<Annotation>, List<Object>> extractMethodAnnotatedParameterValues(
      InvocationContext invocationContext) throws Exception {
    return extractMethodAnnotatedParameterValues(getInterceptedMethodFromContext(invocationContext),
        invocationContext.getParameters());
  }

  /**
   * Provides a centralized way to extract annotated method parameter values.
   * @param method the intercepted method that will be invoked.
   * @return the map with keys of Annotation class and values of object list.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private static Map<Class<Annotation>, List<Object>> extractMethodAnnotatedParameterValues(
      Method method, Object[] parameterValues) throws Exception {

    // Initializing the results
    Map<Class<? extends Annotation>, List<Object>> results = new LinkedHashMap<>();

    Annotation[][] annotations = method.getParameterAnnotations();
    for (int i = 0; i < annotations.length; i++) {
      Annotation[] parameterAnnotations = annotations[i];
      if (parameterAnnotations.length > 0) {
        for (Annotation parameterAnnotation : parameterAnnotations) {
          Object parameterValue = parameterValues[i];
          if (parameterValue != null) {

            if (parameterAnnotation.annotationType().isAssignableFrom(SourcePK.class) ||
                parameterAnnotation.annotationType().isAssignableFrom(TargetPK.class)) {

              if (parameterValue instanceof Collection) {
                for (Object value : ((Collection) parameterValue)) {
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
        }
      }
    }

    return (Map) results;
  }

  /**
   * This method extracts from an object parameter annotated by {@link SourcePK} or {@link
   * TargetPK} the instance of {@link WAPrimaryKey} that it contains. The extracted functional
   * key is added to the given container parameterValue.
   * If the type of the annotated object is not handled, an exception is thrown.
   * @param parameterValues the container of functional key values of parameters.
   * @param annotationClass the class of the annotation that indicate to take in account a method
   * parameter.
   * @param object the instance of the parameter.
   * @throws NotImplementedException when trying to extract a {@link WAPrimaryKey} from an object
   * that is not yet handled. If the object you try to handle is a centralized one,
   * that is to say, used by several components, then you can add the support of this object by
   * this method. If not, try to implement {@link SilverpeasContent} interface.
   */
  private static void addPKParameterValue(
      Map<Class<? extends Annotation>, List<Object>> parameterValues,
      Class<? extends Annotation> annotationClass, Object object) {
    WAPrimaryKey waPrimaryKey = null;
    if (object instanceof Contribution) {
      ContributionIdentifier contributionIdentifier = ((Contribution) object).getContributionId();
      waPrimaryKey =
          new ResourceReference(contributionIdentifier.getLocalId(), contributionIdentifier.getComponentInstanceId());
    } else if (object instanceof SilverpeasContent) {
      SilverpeasContent silverpeasContent = (SilverpeasContent) object;
      waPrimaryKey =
          new ResourceReference(silverpeasContent.getId(), silverpeasContent.getComponentInstanceId());
    } else if (object instanceof SilverContentInterface) {
      SilverContentInterface silverContentInterface = (SilverContentInterface) object;
      waPrimaryKey =
          new ResourceReference(silverContentInterface.getId(), silverContentInterface.getInstanceId());
    } else if (object instanceof PasteDetailFromToPK) {
      PasteDetailFromToPK pasteDetail = (PasteDetailFromToPK) object;
      if (SourcePK.class.equals(annotationClass)) {
        waPrimaryKey = pasteDetail.getFromPK();
      } else {
        waPrimaryKey = pasteDetail.getToPK();
      }
    } else if (object instanceof WAPrimaryKey) {
      waPrimaryKey = (WAPrimaryKey) object;
    } else if (object instanceof SimpleDocument) {
      waPrimaryKey = ((SimpleDocument) object).getPk();
    } else {
      if (object != null) {
        throw new NotImplementedException("");
      }
    }

    if (waPrimaryKey != null) {
      MapUtil.putAddList(parameterValues, annotationClass, waPrimaryKey);
    }
  }

  /**
   * Gets the list of annotation instances from the given list of annotations which the type is
   * the one given.
   * @param annotatedValues container that contains several types of annotation instances.
   * @return List empty or not (but never null)
   */
  @SuppressWarnings("unchecked")
  public static List<Object> getAnnotatedValues(
      Map<Class<Annotation>, List<Object>> annotatedValues,
      Class<? extends Annotation> annotationClass) {
    List<Object> result = null;

    if (annotatedValues != null) {
      result = annotatedValues.get((Class) annotationClass);
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
   * @throws Exception
   */
  private static Method getInterceptedMethodFromContext(InvocationContext invocationContext)
      throws Exception {
    return invocationContext.getMethod();
  }
}