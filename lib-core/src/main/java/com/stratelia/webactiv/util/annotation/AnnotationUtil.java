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
package com.stratelia.webactiv.util.annotation;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.admin.components.PasteDetailFromToPK;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.MapUtil;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.silverpeas.attachment.model.SimpleDocument;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
   * @param invocationContext
   * @return
   * @throws Exception
   */
  public static <A extends Annotation> Map<Class<A>, A> extractMethodAnnotations(
      InvocationContext invocationContext) throws Exception {
    return extractMethodAnnotations(getMethodFromContext(invocationContext));
  }

  /**
   * Provides a centralized way to extract annotation of a method.
   * @param invocationContext
   * @return
   * @throws Exception
   */
  public static <A extends Annotation> Map<Class<A>, A> extractMethodAnnotations(
      ProceedingJoinPoint invocationContext) throws Exception {
    return extractMethodAnnotations(getMethodFromContext(invocationContext));
  }

  /**
   * Provides a centralized way to extract annotation of a method.
   * @param method
   * @param <A>
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private static <A extends Annotation> Map<Class<A>, A> extractMethodAnnotations(Method method)
      throws Exception {

    // Initializing the results
    Map<Class<? extends Annotation>, Annotation> results =
        new LinkedHashMap<Class<? extends Annotation>, Annotation>();


    for (Annotation annotation : method.getAnnotations()) {
      results.put(annotation.annotationType(), annotation);
    }

    return (Map) results;
  }

  /**
   * Provides a centralized way to extract annoted method parameter values.
   * @param invocationContext
   * @return
   * @throws Exception
   */
  public static Map<Class<Annotation>, List<Object>> extractMethodAnnotedParameterValues(
      InvocationContext invocationContext) throws Exception {
    return extractMethodAnnotedParameterValues(getMethodFromContext(invocationContext),
        invocationContext.getParameters());
  }

  /**
   * Provides a centralized way to extract annoted method parameter values.
   * @param invocationContext
   * @return
   * @throws Exception
   */
  public static Map<Class<Annotation>, List<Object>> extractMethodAnnotedParameterValues(
      ProceedingJoinPoint invocationContext) throws Exception {
    return extractMethodAnnotedParameterValues(getMethodFromContext(invocationContext),
        invocationContext.getArgs());
  }

  /**
   * Provides a centralized way to extract annoted method parameter values.
   * @param method
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private static Map<Class<Annotation>, List<Object>> extractMethodAnnotedParameterValues(
      Method method, Object[] parameterValues) throws Exception {

    // Initializing the results
    Map<Class<? extends Annotation>, List<Object>> results =
        new LinkedHashMap<Class<? extends Annotation>, List<Object>>();

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

            } else if (parameterAnnotation.annotationType().isAssignableFrom(TargetObject.class)) {

              MapUtil.putAddList(results, parameterAnnotation.annotationType(), parameterValue);

            } else if (parameterAnnotation.annotationType().isAssignableFrom(Language.class)) {

              String language = null;
              if (parameterValue instanceof String) {
                language = (String) parameterValue;
              } else if (parameterValue instanceof Locale) {
                language = ((Locale) parameterValue).getLanguage();
              }
              MapUtil.putAddList(results, parameterAnnotation.annotationType(), language);

            }
          }
        }
      }
    }

    return (Map) results;
  }

  /**
   * Perform pk parameter value
   * @param parameterValues
   * @param annotationClass
   * @param object
   */
  private static void addPKParameterValue(
      Map<Class<? extends Annotation>, List<Object>> parameterValues,
      Class<? extends Annotation> annotationClass, Object object) {
    WAPrimaryKey waPrimaryKey = null;
    if (object instanceof SilverpeasContent) {
      SilverpeasContent silverpeasContent = (SilverpeasContent) object;
      waPrimaryKey =
          new ForeignPK(silverpeasContent.getId(), silverpeasContent.getComponentInstanceId());
    } else if (object instanceof SilverContentInterface) {
      SilverContentInterface silverContentInterface = (SilverContentInterface) object;
      waPrimaryKey =
          new ForeignPK(silverContentInterface.getId(), silverContentInterface.getInstanceId());
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
        throw new NotImplementedException();
      }
    }

    if (waPrimaryKey != null) {
      MapUtil.putAddList(parameterValues, annotationClass, waPrimaryKey);
    }
  }

  /**
   * Gets the values associated to an annotation.
   * @param annotedValues
   * @return List empty or not (but never null)
   */
  @SuppressWarnings("unchecked")
  public static List<Object> getAnnotedValues(Map<Class<Annotation>, List<Object>> annotedValues,
      Class<? extends Annotation> annotationClass) {
    List<Object> result = null;

    if (annotedValues != null) {
      result = annotedValues.get((Class) annotationClass);
    }

    if (result == null) {
      result = new ArrayList<Object>(0);
    }
    return result;
  }

  /**
   * Provides a centralized way to extract annotation of a method.
   * @param invocationContext
   * @return
   * @throws Exception
   */
  private static Method getMethodFromContext(Object invocationContext) throws Exception {
    if (invocationContext instanceof ProceedingJoinPoint) {
      ProceedingJoinPoint context = (ProceedingJoinPoint) invocationContext;
      final String methodName = context.getSignature().getName();
      final MethodSignature methodSignature = (MethodSignature) context.getSignature();
      Method method = methodSignature.getMethod();
      if (method.getDeclaringClass().isInterface()) {
        method = context.getTarget().getClass()
            .getDeclaredMethod(methodName, method.getParameterTypes());
      }
      return method;
    } else if (invocationContext instanceof InvocationContext) {
      return ((InvocationContext) invocationContext).getMethod();
    }
    throw new NotImplementedException();
  }
}