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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.jetbrains.annotations.Nullable;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.ServletRequest;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This class decodes the request parameters in order to set them to a simple specified POJO which
 * attributes are annotated by {@link javax.ws.rs.FormParam} or
 * {@link javax.xml.bind.annotation.XmlElement} annotation.<br>
 * It is possible to annotate the attribute with {@link UnescapeHtml} annotation in order to
 * perform an HTML unescape operation (string type only).<br>
 * WARNINGS:
 * <ul>
 * <li>for now, the decoder is not compatible with Inner Classes</li>
 * <li>specified default value on {@link javax.xml.bind.annotation.XmlElement} is not yet
 * handled</li>
 * </ul>
 * @author Yohann Chastagnier
 */
public class RequestParameterDecoder {

  // Singleton (Could be useful for Unit Tests)
  private static final RequestParameterDecoder decoder = new RequestParameterDecoder();

  private static final String XML_ELEMENT_DEFAULT_NAME_VALUE = "##default";

  /**
   * Gets the singleton instance.
   * @return the singleton instance.
   */
  protected static RequestParameterDecoder getInstance() {
    return decoder;
  }

  /**
   * Decodes the request parameters in order to return an object filled with their values.
   * @param request the {@link ServletRequest} that will be wrapped by {@link HttpRequest}
   * @param objectClass the class of the requested returned instance.
   * @param <O> the type of the requested returned instance.
   * @return the decoded specified entity.
   */
  public static <O> O decode(ServletRequest request, Class<O> objectClass) {
    return decode(HttpRequest.decorate(request), objectClass);
  }

  /**
   * Decodes the request parameters in order to return an object filled with their values.
   * @param request the {@link HttpRequest} wrapper that handle efficiently all parameters,
   * included
   * those of multipart request type.
   * @param objectClass the class of the requested returned instance.
   * @param <O> the type of the requested returned instance.
   * @return the decoded specified entity.
   */
  public static <O> O decode(HttpRequest request, Class<O> objectClass) {
    return getInstance().decodeReq(request, objectClass);
  }

  public static <T> OffsetDateTime asOffsetDateTime(T object) {
    if (object instanceof OffsetDateTime) {
      return (OffsetDateTime) object;
    } else if (object instanceof String) {
      String typedObject = ((String) object).trim();
      return OffsetDateTime.parse(typedObject);
    }
    if (object != null) {
      throw new IllegalArgumentException();
    }
    return null;
  }

  public static <T> boolean asBoolean(T object) {
    if (object instanceof Boolean) {
      return (Boolean) object;
    } else if (object instanceof String) {
      String typedObject = ((String) object).trim();
      return StringUtil.getBooleanValue(typedObject);
    }
    if (object != null) {
      throw new IllegalArgumentException();
    }
    return false;
  }

  public static <T> Long asLong(T object) {
    if (object instanceof Number) {
      return ((Number) object).longValue();
    } else if (object instanceof String) {
      String typedObject = ((String) object).trim();
      if (StringUtil.isLong(typedObject)) {
        return Long.valueOf(typedObject);
      }
      return null;
    }
    if (object != null) {
      throw new IllegalArgumentException();
    }
    return null;
  }

  public static <T> Integer asInteger(T object) {
    if (object instanceof Number) {
      return ((Number) object).intValue();
    } else if (object instanceof String) {
      String typedObject = ((String) object).trim();
      if (StringUtil.isInteger(typedObject)) {
        return Integer.valueOf(typedObject);
      }
      return null;
    }
    if (object != null) {
      throw new IllegalArgumentException();
    }
    return null;
  }

  public static <T> Date asDate(T date, T hour, String userLanguage) throws ParseException {
    if (date instanceof String) {
      String typedDate = (String) date;
      String typedHour = (String) hour;
      if (StringUtil.isDefined(typedDate)) {
        return DateUtil.stringToDate(typedDate, typedHour, userLanguage);
      }
      return null;
    }
    if (date != null) {
      throw new IllegalArgumentException();
    }
    return null;
  }

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  public static <E extends Enum<E>> E asEnum(String enumValue, Class<E> enumClass) {
    Method fromMethod = null;

    for (Method method : enumClass.getMethods()) {
      Class<?>[] methodParameterTypes = method.getParameterTypes();
      if (method.getAnnotation(JsonCreator.class) != null && methodParameterTypes.length == 1 &&
          methodParameterTypes[0].isAssignableFrom(String.class)) {
        fromMethod = method;
        break;
      }
    }

    if (fromMethod == null) {
      try {
        fromMethod = enumClass.getMethod("valueOf", String.class);
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    try {
      return (E) fromMethod.invoke(null, enumValue);
    } catch (Exception e) {
      SilverLogger.getLogger(RequestParameterDecoder.class).warn(e);
    }

    return null;
  }

  /**
   * The private implementation.
   */
  private <O> O decodeReq(HttpRequest request, Class<O> objectClass) {
    try {

      // New instance
      Constructor<O> constructor = objectClass.getDeclaredConstructor();
      constructor.trySetAccessible();
      O newInstance = constructor.newInstance();

      // Reading all class fields.
      for (Field field : objectClass.getDeclaredFields()) {
        final String paramName;
        paramName = findParameterName(field);
        if (paramName != null) {
          final Object value = findParameterValue(field, paramName, request);
          if (!field.getType().isPrimitive() || value != null) {
            field.trySetAccessible();
            field.set(newInstance, value);
          }
        }
      }

      // Instance is set.
      // Returning it.
      return newInstance;
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Nullable
  private Object findParameterValue(final Field field, final String paramName,
      final HttpRequest request)
      throws ParseException {
    final boolean unescapeHtml = field.getAnnotation(UnescapeHtml.class) != null;
    Class<?> parameterClass = field.getType();
    final Object value;
    if (Collection.class.isAssignableFrom(parameterClass)) {
      final Collection<?> values;
      if (parameterClass.isAssignableFrom(Set.class)) {
        values = new HashSet<>();
      } else {
        values = new ArrayList<>();
      }
      Class<?> collectionHandledType =
          (Class<?>) ((ParameterizedType) field.getAnnotatedType().getType())
              .getActualTypeArguments()[0];
      value = getParameterValues(values, request.getParameterValues(paramName),
          collectionHandledType, unescapeHtml);
    } else {
      value = getParameterValue(request, paramName, parameterClass, unescapeHtml);
    }
    return value;
  }

  @Nullable
  private String findParameterName(final Field field) {
    final String paramName;
    // Is existing the FormParam annotation ?
    FormParam formParam = field.getAnnotation(FormParam.class);
    if (formParam != null) {
      paramName = StringUtil.isDefined(formParam.value()) ? formParam.value() : field.getName();
    } else {
      // Is existing the XmlElement annotation ?
      XmlElement xmlParam = field.getAnnotation(XmlElement.class);
      if (xmlParam != null) {
        paramName = !XML_ELEMENT_DEFAULT_NAME_VALUE.equals(xmlParam.name()) ? xmlParam.name() :
            field.getName();
      } else {
        // No class attribute for parameter
        paramName = null;
      }
    }
    return paramName;
  }

  /**
   * Gets the parameter value according to the type of the specified field.
   * @param request the current request.
   * @param parameterName the current parameter name to verify.
   * @param parameterClass the class into which the parameter value must be converted.
   * @return the decoded parameter value.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private Object getParameterValue(HttpRequest request, String parameterName,
      Class<?> parameterClass, boolean unescapeHtml) throws ParseException {
    final Object value;
    if (parameterClass.isAssignableFrom(RequestFile.class)) {
      value = request.getParameterAsRequestFile(parameterName);
    } else if (parameterClass.isAssignableFrom(Date.class)) {
      value = request.getParameterAsDate(parameterName);
    } else {
      value = getValueAs(request.getParameter(parameterName), parameterClass, unescapeHtml);
    }
    return value;
  }

  private Object getParameterValues(Collection<?> finalValues, String[] values,
      Class<?> parameterClass, boolean unescapeHtml) {
    if (values != null) {
      for (String value : values) {
        finalValues.add(getValueAs(value, parameterClass, unescapeHtml));
      }
      return finalValues;
    }
    return null;
  }

  /**
   * Gets the  value according to the type of the specified field.
   * @param parameterValue the value to get as.
   * @param parameterClass the class into which the parameter value must be converted.
   * @return the decoded value.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private <T> T getValueAs(String parameterValue, Class<?> parameterClass, boolean unescapeHtml) {
    final Object value;
    if (parameterClass.isAssignableFrom(String.class)) {
      value = asString(parameterValue, unescapeHtml);
    } else if (parameterClass.isAssignableFrom(OffsetDateTime.class)) {
      value = asOffsetDateTime(parameterValue);
    } else if (parameterClass.isAssignableFrom(Long.class)) {
      value = asLong(parameterValue);
    } else if (parameterClass.isAssignableFrom(Long.TYPE)) {
      value = asLong(parameterValue);
    } else if (parameterClass.isAssignableFrom(Integer.class)) {
      value = asInteger(parameterValue);
    } else if (parameterClass.isAssignableFrom(Integer.TYPE)) {
      value = asInteger(parameterValue);
    } else if (parameterClass.isAssignableFrom(Boolean.class)) {
      value = parameterValue != null ? asBoolean(parameterValue) : null;
    } else if (parameterClass.isAssignableFrom(Boolean.TYPE)) {
      value = asBoolean(parameterValue);
    } else if (parameterClass.isEnum()) {
      value = asEnum(parameterValue, (Class) parameterClass);
    } else if (parameterClass.isAssignableFrom(URI.class)) {
      value = asURI(parameterValue);
    } else {
      throw new UnsupportedOperationException(
          "The type " + parameterClass.getName() + " is not handled...");
    }
    return (T) value;
  }

  @Nullable
  private Object asURI(final String parameterValue) {
    final Object value;
    if (StringUtil.isDefined(parameterValue)) {
      value = URI.create(parameterValue);
    } else {
      value = null;
    }
    return value;
  }

  private Object asString(final String parameterValue, final boolean unescapeHtml) {
    final Object value;
    if (unescapeHtml) {
      value = WebEncodeHelper.htmlStringToJavaString(parameterValue);
    } else {
      value = parameterValue;
    }
    return value;
  }
}
