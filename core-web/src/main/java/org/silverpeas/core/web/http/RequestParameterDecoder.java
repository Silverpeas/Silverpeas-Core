/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.web.http;

import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.ServletRequest;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Date;

/**
 * This class decodes the request parameters in order to set them to a simple specified POJO which
 * attributes are annotated by {@link javax.ws.rs.FormParam} or
 * {@link javax.xml.bind.annotation.XmlElement} annotation.<br/>
 * It is possible to annotate the attribute with {@link UnescapeHtml} annotation in order to
 * perform an HTML unescape operation (string type only).<br/>
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
   * @param <OBJECT> the type of the requested returned instance.
   * @return the decoded specified entity.
   */
  public static <OBJECT> OBJECT decode(ServletRequest request, Class<OBJECT> objectClass) {
    return decode(HttpRequest.decorate(request), objectClass);
  }

  /**
   * Decodes the request parameters in order to return an object filled with their values.
   * @param request the {@link HttpRequest} wrapper that handle efficiently all parameters,
   * included
   * those of multipart request type.
   * @param objectClass the class of the requested returned instance.
   * @param <OBJECT> the type of the requested returned instance.
   * @return the decoded specified entity.
   */
  public static <OBJECT> OBJECT decode(HttpRequest request, Class<OBJECT> objectClass) {
    return getInstance()._decode(request, objectClass);
  }

  /**
   * The private implementation.
   */
  private <OBJECT> OBJECT _decode(HttpRequest request, Class<OBJECT> objectClass) {
    try {

      // New instance
      Constructor<OBJECT> constructor = objectClass.getDeclaredConstructor();
      constructor.setAccessible(true);
      OBJECT newInstance = constructor.newInstance();

      // Reading all class fields.
      for (Field field : objectClass.getDeclaredFields()) {
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
        if (paramName != null) {
          field.setAccessible(true);
          Object value = getParameterValue(request, paramName, field.getType(),
              field.getAnnotation(UnescapeHtml.class) != null);
          if (!field.getType().isPrimitive() || value != null) {
            field.set(newInstance, value);
          }
        }
      }

      // Instance is set.
      // Returning it.
      return newInstance;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
      Class<?> parameterClass, boolean unescapeHtml) throws Exception {
    final Object value;
    if (parameterClass.isAssignableFrom(RequestFile.class)) {
      value = request.getParameterAsRequestFile(parameterName);
    } else if (parameterClass.isAssignableFrom(String.class)) {
      if (unescapeHtml) {
        value = EncodeHelper.htmlStringToJavaString(request.getParameter(parameterName));
      } else {
        value = request.getParameter(parameterName);
      }
    } else if (parameterClass.isAssignableFrom(Long.class)) {
      value = request.getParameterAsLong(parameterName);
    } else if (parameterClass.getName().equals("long")) {
      value = request.getParameterAsLong(parameterName);
    } else if (parameterClass.isAssignableFrom(Integer.class)) {
      value = request.getParameterAsInteger(parameterName);
    } else if (parameterClass.getName().equals("int")) {
      value = request.getParameterAsInteger(parameterName);
    } else if (parameterClass.isAssignableFrom(Date.class)) {
      value = request.getParameterAsDate(parameterName);
    } else if (parameterClass.isAssignableFrom(Boolean.class)) {
      value = request.getParameterAsBoolean(parameterName);
    } else if (parameterClass.getName().equals("boolean")) {
      value = request.getParameterAsBoolean(parameterName);
    } else if (parameterClass.isEnum()) {
      value = request.getParameterAsEnum(parameterName, (Class) parameterClass);
    } else if (parameterClass.isAssignableFrom(URI.class)) {
      if (StringUtil.isDefined(request.getParameter(parameterName))) {
        value = URI.create(request.getParameter(parameterName));
      } else {
        value = null;
      }
    } else {
      throw new UnsupportedOperationException(
          "The type " + parameterClass.getName() + " is not handled...");
    }
    return value;
  }
}
