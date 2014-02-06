/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.NotImplementedException;

import static com.stratelia.silverpeas.peasCore.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

/**
 * An HTTP request decorating an HTTP servlet request with some additional methods and by changing
 * the implementation of some of its methods to take into account some Silverpeas specificities or
 * needs. For example, the <code>getParameter(java.lang.String)</code> method has been modified to
 * take into account also the parameters passed in a multipart/form-data stream.
 *
 * @author mmoquillon
 */
public class HttpRequest extends HttpServletRequestWrapper {

  private List<FileItem> fileItems = null;

  private HttpRequest(HttpServletRequest request) {
    super(request);
  }

  /**
   * Decorates the specified HTTP servlet request with an HttpRequest instance. If the request is
   * already an HttpRequest instance, then it is simply returned.
   *
   * @param request the Http servlet request to decorate.
   * @return an HttpRequest instance decorating the specified request.
   */
  public static HttpRequest decorate(final HttpServletRequest request) {
    return request instanceof HttpRequest ? (HttpRequest) request : new HttpRequest(request);
  }

  /**
   * Decorates the specified servlet request with an HttpRequest instance. If the request is already
   * an HttpRequest instance, then it is simply returned.
   *
   * @param request the servlet request to decorate. Must be of type HttpServletRequest.
   * @return an HttpRequest instance decorating the specified request.
   */
  public static HttpRequest decorate(final ServletRequest request) {
    return decorate((HttpServletRequest) request);
  }

  /**
   * Is this request within an anonymous user session?
   *
   * @return true if the request is sent in the context of an opened user session and this session
   * is for an anonymous user.
   */
  public boolean isWithinAnonymousUserSession() {
    MainSessionController controller = getMainSessionController();
    return controller != null && controller.getCurrentUserDetail().isAnonymous();
  }

  /**
   * Is this request within an opened user session?
   *
   * @return true if the request is sent in the context of a Silvepreas user session.
   */
  public boolean isWithinUserSession() {
    return getMainSessionController() != null;
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
   * multipart/form-data stream.
   *
   * @return a list of FileItem instances parsed from the request, in the order that they were
   * transmitted.
   */
  public List<FileItem> getFileItems() {
    return (fileItems == null ? fileItems = FileUploadUtil.parseRequest(this) : fileItems);
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
   * multipart/form-data stream and returns the item whose the name matches the specified one.
   *
   * @param name the name of the data to fetch.
   * @return the FileItem instance whose the name matches the specified one or null if no such data
   * exists in the multipart/form-data stream. The file item can be either a file or a parameter.
   */
  public FileItem getFileItem(String name) {
    FileItem item = null;
    List<FileItem> items = getFileItems();
    for (FileItem fileItem : items) {
      if (fileItem.getFieldName().equals(name)) {
        item = fileItem;
        break;
      }
    }
    return item;
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
   * multipart/form-data stream and returns the item whose the type is a file and the name matches
   * the specified one.
   * @param name the name of the file item to fetch.
   * @return the file item whose the name matches the specified one or null if no such data exists
   * in the multipart stream.
   */
  public FileItem getFile(String name) {
    return FileUploadUtil.getFile(getFileItems(), name);
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
   * multipart/form-data stream and returns the item whose the type is a file. If there is several
   * items that represent a file, then only the first one is returned.
   * @return the file item representing a file in the multipart stream or null if no such data
   * exists in the multipart stream.
   */
  public FileItem getSingleFile() {
    return FileUploadUtil.getFile(getFileItems());
  }

  /**
   * Is this request has a cookie with the specified name?
   *
   * @param name the name of the cookie.
   * @return true if a cookie with the specified name is carried by this request, false otherwise.
   */
  public boolean hasCookie(String name) {
    if (StringUtil.isNotDefined(name)) {
      return false;
    }
    boolean found = false;
    Cookie[] cookies = getCookies();
    for (int i = 0; i < cookies.length && !found; i++) {
      found = cookies[i].getName().equals(name);
    }
    return found;
  }

  @Override
  public int getServerPort() {
    return GeneralPropertiesManager.getInteger("server.http.port", super.getServerPort());
  }

  @Override
  public boolean isSecure() {
    return !GeneralPropertiesManager.getBoolean("server.mixed", false) && (super.isSecure()
        || GeneralPropertiesManager.getBoolean("server.ssl", false));
  }

  /**
   * Gets the main controller in the current user session. If the current session doesn't match an
   * opened user session in Silverpeas, then null is returned.
   *
   * @return the main session controller mapped with this request.
   */
  public MainSessionController getMainSessionController() {
    HttpSession session = getSession(false);
    if (session != null) {
      return (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
    }
    return null;
  }

  /**
   * Gets the language of the user behind this request.
   *
   * @return the language of the user as he has chosen in its profile in Silverpeas.
   */
  public String getUserLanguage() {
    String language = I18NHelper.defaultLanguage;
    MainSessionController mainSessionController = getMainSessionController();
    if (mainSessionController != null) {
      language = mainSessionController.getFavoriteLanguage();
    }
    return language;
  }

  /**
   * Get a parameter value as a Long.
   *
   * @param attributeName the name of the attribute.
   * @return the value of the attribute as a long.
   */
  public Long getAttributeAsLong(String attributeName) {
    return asLong(getAttribute(attributeName));
  }

  /**
   * Returns an array of String objects containing all of the values the given request parameter
   * has, or null if the parameter does not exist. The parameters from a multipart/form-data stream
   * are also considered by this method, unlike of the default behavior of the decorated request.
   *
   * If the parameter has a single value, the array has a length of 1.
   *
   * @param name the name of the parameter whose value is requested.
   * @return an array of String objects containing the parameter's values.
   */
  @Override
  public String[] getParameterValues(String name) {
    String[] values = super.getParameterValues(name);
    if (values == null && isContentInMultipart()) {
      values = new String[]{FileUploadUtil.getParameter(getFileItems(), name, null,
          getCharacterEncoding())};
    }
    return values;
  }

  /**
   * Returns an Enumeration of String objects containing the names of the parameters contained in
   * this request. If the request has no parameters, the method returns an empty Enumeration. The
   * parameters from a multipart/form-data stream are also considered by this method, unlike of the
   * default behavior of the decorated request.
   *
   * @return an Enumeration of String objects, each String containing the name of a request
   * parameter; or an empty Enumeration if the request has no parameters.
   */
  @Override
  public Enumeration<String> getParameterNames() {
    Enumeration<String> names = super.getParameterNames();
    if (!names.hasMoreElements() && isContentInMultipart()) {
      List<FileItem> items = getFileItems();
      List<String> itemNames = new ArrayList<String>(items.size());
      for (FileItem item : items) {
        if (item.isFormField()) {
          itemNames.add(item.getFieldName());
        }
      }
      names = Collections.enumeration(itemNames);
    }
    return names;
  }

  /**
   * Returns a java.util.Map of the parameters of this request.
   *
   * Request parameters are extra information sent with the request. For HTTP servlets, parameters
   * are contained in the query string or posted form data. The parameters from a
   * multipart/form-data stream are also considered by this method, unlike of the default behavior
   * of the decorated request.
   *
   * @return an immutable java.util.Map containing parameter names as keys and parameter values as
   * map values. The keys in the parameter map are of type String. The values in the parameter map
   * are of type String array.
   */
  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> map = super.getParameterMap();
    if (map.isEmpty() && isContentInMultipart()) {
      List<FileItem> items = getFileItems();
      map = new HashMap<String, String[]>(items.size());
      for (FileItem item : items) {
        if (item.isFormField()) {
          String[] value;
          try {
            value = new String[]{item.getString(getCharacterEncoding())};
          } catch (UnsupportedEncodingException ex) {
            value = new String[]{item.getString()};
          }
          map.put(item.getFieldName(), value);
        }
      }
      map = Collections.unmodifiableMap(map);
    }
    return map;
  }

  /**
   * Returns the value of a request parameter as a String, or null if the parameter does not exist.
   * Request parameters are extra information sent with the request. For HTTP servlets, parameters
   * are contained in the query string or posted form data. The parameters from a
   * multipart/form-data stream are also considered by this method, unlike of the default behavior
   * of the decorated request.
   *
   * You should only use this method when you are sure the parameter has only one value. If the
   * parameter might have more than one value, use getParameterValues(java.lang.String).
   *
   * If you use this method with a multivalued parameter, the value returned is equal to the first
   * value in the array returned by getParameterValues.
   *
   * If the parameter data was sent in the request body, such as occurs with an HTTP POST request,
   * then reading the body directly via getInputStream() or getReader() can interfere with the
   * execution of this method.
   *
   * @param name the name of the parameter.
   * @return the single value of the parameter.
   */
  @Override
  public String getParameter(String name) {
    String value = super.getParameter(name);
    if (value == null && isContentInMultipart()) {
      value = FileUploadUtil.getParameter(getFileItems(), name, null, getCharacterEncoding());
    }
    return value;
  }

  /**
   * Get a parameter value as a boolean.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a boolean.
   */
  public boolean getParameterAsBoolean(String parameterName) {
    return asBoolean(getParameter(parameterName));
  }

  /**
   * Get a parameter value as a Long.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a long.
   */
  public Long getParameterAsLong(String parameterName) {
    return asLong(getParameter(parameterName));
  }

  /**
   * Get a date from a date parameter.
   *
   * @param dateParameterName the name of the parameter.
   * @return the value of the parameter as a date.
   * @throws java.text.ParseException if the parameter value isn't a date.
   */
  public Date getParameterAsDate(String dateParameterName) throws ParseException {
    return asDate(getParameter(dateParameterName), null);
  }

  /**
   * Get a date from one date parameter and one hour parameter.
   *
   * @param dateParameterName the name of the date parameter.
   * @param hourParameterName the name of the time parameter.
   * @return the value of the parameter as a date.
   * @throws java.text.ParseException if the parameter value isn't a date.
   */
  public Date getParameterAsDate(String dateParameterName, String hourParameterName)
      throws ParseException {
    return asDate(getParameter(dateParameterName), getParameter(hourParameterName));
  }

  private <T> boolean asBoolean(T object) {
    if (object instanceof Boolean) {
      return (Boolean) object;
    } else if (object instanceof String) {
      String typedObject = (String) object;
      return StringUtil.getBooleanValue(typedObject);
    }
    if (object != null) {
      throw new NotImplementedException();
    }
    return false;
  }

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

  private <T> Date asDate(T date, T hour) throws ParseException {
    if (date instanceof String) {
      String typedDate = (String) date;
      String typedHour = (String) hour;
      if (StringUtil.isDefined(typedDate)) {
        return DateUtil.stringToDate(typedDate, typedHour, getUserLanguage());
      }
      return null;
    }
    if (date != null) {
      throw new NotImplementedException();
    }
    return null;
  }

  /**
   * Is the content in this request is encoded in a multipart stream.
   * @return true if the content type of this request is a compilant multipart/form-data stream,
   * false otherwise.
   */
  public boolean isContentInMultipart() {
    return FileUploadUtil.isRequestMultipart(this);
  }

  @Override
  public String getCharacterEncoding() {
    String encoding = super.getCharacterEncoding();
    if (StringUtil.isNotDefined(encoding)) {
      encoding = FileUploadUtil.DEFAULT_ENCODING;
    }
    return encoding;
  }
}
