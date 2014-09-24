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
package org.silverpeas.servlet;

import org.silverpeas.util.StringUtil;

import javax.servlet.ServletRequest;
import javax.ws.rs.FormParam;
import java.lang.reflect.Field;
import java.util.Date;

/**
 * This class decodes the request parameters in order to set them to a simple specified POJO which
 * attributes are annotated by {@link javax.ws.rs.FormParam} annotation.
 * WARNING : for now, the decoder is not compatible with Inner Classes.
 * @author: Yohann Chastagnier
 */
public class RequestParameterDecoder {

  // Singleton (Could be useful for Unit Tests)
  private static final RequestParameterDecoder decoder = new RequestParameterDecoder();

  /**
   * Gets the singleton instance.
   * @return
   */
  protected static RequestParameterDecoder getInstance() {
    return decoder;
  }

  /**
   * Decodes the request parameters in order to return an object filled with their values.
   * @param request the {@link ServletRequest} that will be wrapped by {@link HttpRequest}
   * @param objectClass the class of the requested returned instance.
   * @param <OBJECT> the type of the requested returned instance.
   * @return
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
   * @return
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
      OBJECT newInstance = objectClass.newInstance();

      // Reading all class fields.
      for (Field field : objectClass.getDeclaredFields()) {
        // Is existing the FormParam annotation ?
        FormParam param = field.getAnnotation(FormParam.class);
        if (param != null) {
          boolean isAccessible = field.isAccessible();
          if (!isAccessible) {
            field.setAccessible(true);
          }
          field.set(newInstance, getParameterValue(request,
              StringUtil.isDefined(param.value()) ? param.value() : field.getName(),
              field.getType()));
          if (!isAccessible) {
            field.setAccessible(false);
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
   * @param request
   * @param parameterName
   * @param parameterClass
   * @return
   * @throws Exception
   */
  private Object getParameterValue(HttpRequest request, String parameterName,
      Class<?> parameterClass) throws Exception {
    final Object value;
    if (parameterClass.isAssignableFrom(RequestFile.class)) {
      value = request.getParameterAsRequestFile(parameterName);
    } else if (parameterClass.isAssignableFrom(String.class)) {
      value = request.getParameter(parameterName);
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
    } else {
      throw new UnsupportedOperationException(
          "The type " + parameterClass.getName() + " is not handled...");
    }
    return value;
  }
}
