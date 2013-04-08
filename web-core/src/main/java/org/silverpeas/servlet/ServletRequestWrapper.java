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
package org.silverpeas.servlet;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DateUtil;
import org.apache.commons.lang.NotImplementedException;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.util.Date;

/**
 * User: Yohann Chastagnier
 * Date: 05/04/13
 */
public class ServletRequestWrapper {

  private final ServletRequest request;
  private final String language;

  /**
   * Default constructor.
   * @param request
   */
  public ServletRequestWrapper(final ServletRequest request, final String language) {
    this.request = request;
    this.language = language;
  }

  protected ServletRequest getRequest() {
    return request;
  }

  public String getLanguage() {
    return language;
  }

  /**
   * Set an attribute.
   * @param attributeName
   * @param object
   */
  public void setAttribute(String attributeName, Object object) {
    request.setAttribute(attributeName, object);
  }

  /**
   * Get a attribute value as a String.
   * @param attributeName
   * @return
   */
  public Object getAttribute(String attributeName) {
    return request.getAttribute(attributeName);
  }

  /**
   * Get a parameter value as a Long.
   * @param attributeName
   * @return
   */
  public Long getAttributeAsLong(String attributeName) {
    return asLong(getAttribute(attributeName));
  }

  /**
   * Get a parameter value as a String.
   * @param parameterName
   * @return
   */
  public String getParameter(String parameterName) {
    return request.getParameter(parameterName);
  }

  /**
   * Get a parameter value as a Long.
   * @param parameterName
   * @return
   */
  public Long getParameterAsLong(String parameterName) {
    return asLong(getParameter(parameterName));
  }

  /**
   * Get a date from one date parameter and one hour parameter.
   * @param dateParameterName
   * @param hourParameterName
   * @return
   */
  public Date getParameterAsDate(String dateParameterName, String hourParameterName)
      throws ParseException {
    return asDate(getParameter(dateParameterName), getParameter(hourParameterName));
  }

  /**
   * Centralizition of Object to Long convertion.
   * @param object
   * @return
   */
  private <T> Long asLong(T object) {
    if (object instanceof Number) {
      return ((Number) object).longValue();
    } else if (object instanceof String) {
      String typedObject = (String) object;
      if (StringUtil.isLong(typedObject)) {
        return Long.valueOf(typedObject);
      }
      return null;
    }
    if (object != null) {
      throw new NotImplementedException();
    }
    return null;
  }

  /**
   * Centralizition of Object to Long convertion.
   * @param date
   * @param hour
   * @return
   */
  private <T> Date asDate(T date, T hour) throws ParseException {
    if (date instanceof String) {
      String typedDate = (String) date;
      String typedHour = (String) hour;
      if (StringUtil.isDefined(typedDate)) {
        return DateUtil.stringToDate(typedDate, typedHour, language);
      }
      return null;
    }
    if (date != null) {
      throw new NotImplementedException();
    }
    return null;
  }
}
