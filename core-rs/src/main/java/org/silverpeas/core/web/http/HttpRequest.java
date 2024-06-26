/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describing
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
 *
 */
package org.silverpeas.core.web.http;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.io.upload.FileUploadManager;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.split;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;

/**
 * An HTTP request decorating an HTTP servlet request with some additional methods and by changing
 * the implementation of some of its methods to take into account some Silverpeas specificities or
 * needs. For example, the <code>getParameter(java.lang.String)</code> method has been modified to
 * take into account also the parameters passed in a multipart/form-data stream.
 * @author mmoquillon
 */
public class HttpRequest extends HttpServletRequestWrapper {

  private static final String SECURE_PROPERTY = "secure";

  private List<FileItem> fileItems = null;

  /**
   * Constructs a new {@link HttpRequest} by decorating the specified {@link HttpServletRequest}.
   * @param request the Http servlet request to decorate.
   */
  protected HttpRequest(HttpServletRequest request) {
    super(request);
    // The decorated request is put into attributes in order to provide it to the REST web
    // services that deals with proxies...
    request.setAttribute(HttpRequest.class.getName(), this);
  }

  /**
   * Constructs a new copy of the specified {@link HttpRequest}. Useful for {@link HttpRequest}
   * subclasses to copy all the HttpRequest attributes and hence keep the up-to-date their state
   * with the {@link HttpRequest} source.
   * @param request the {@link HttpRequest} to copy.
   */
  protected HttpRequest(HttpRequest request) {
    super((HttpServletRequest) request.getRequest());
    this.fileItems = request.fileItems;
  }

  /**
   * Decorates the specified HTTP servlet request with an HttpRequest instance. If the request is
   * already an HttpRequest instance, then it is simply returned.
   * @param request the Http servlet request to decorate.
   * @return an HttpRequest instance decorating the specified request.
   */
  public static HttpRequest decorate(final HttpServletRequest request) {
    CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .put(SECURE_PROPERTY, request.isSecure());
    return request instanceof HttpRequest ? (HttpRequest) request : new HttpRequest(request);
  }

  /**
   * Decorates the specified servlet request with an HttpRequest instance. If the request is already
   * an HttpRequest instance, then it is simply returned.
   * @param request the servlet request to decorate. Must be of type HttpServletRequest.
   * @return an HttpRequest instance decorating the specified request.
   */
  public static HttpRequest decorate(final ServletRequest request) {
    return decorate((HttpServletRequest) request);
  }

  /**
   * Is the HTTP request currently processed is secure (that is carried through a TLS connection)?
   * @return true of the current HTTP request is secure, false otherwise.
   */
  public static boolean isCurrentRequestSecure() {
    Boolean secure = CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .get(SECURE_PROPERTY, Boolean.class);
    if (secure == null) {
      throw new IllegalStateException(
          "The current execution context isn't relative to an incoming HTTP request");
    }
    return secure;
  }

  /**
   * Is this request within an anonymous user session?
   * @return true if the request is sent in the context of an opened user session and this session
   * is for an anonymous user.
   */
  public boolean isWithinAnonymousUserSession() {
    User user = User.getCurrentRequester();
    return user != null && user.isAnonymous();
  }

  /**
   * Is this request within an opened user session?
   * @return true if the request is sent in the context of a Silverpeas user session.
   */
  public boolean isWithinUserSession() {
    return User.getCurrentRequester() != null;
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
   * multipart/form-data stream.
   * @return a list of FileItem instances parsed from the request, in the order that they were
   * transmitted.
   */
  public List<FileItem> getFileItems() {
    if (fileItems == null) {
      fileItems = FileUploadUtil.parseRequest(this);
    }
    return fileItems;
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
   * multipart/form-data stream and returns the item whose type is a file and the name matches
   * the specified one.
   * @param name the name of the file item to fetch.
   * @return the file item whose name matches the specified one or null if no such data exists
   * in the multipart stream.
   */
  public FileItem getFile(String name) {
    return FileUploadUtil.getFile(getFileItems(), name);
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
   * multipart/form-data stream and returns the item whose type is a file. If there is several
   * items that represent a file, then only the first one is returned.
   * @return the file item representing a file in the multipart stream or null if no such data
   * exists in the multipart stream.
   */
  public FileItem getSingleFile() {
    return FileUploadUtil.getFile(getFileItems());
  }

  /**
   * Gets the language of the user behind this request.
   * @return the language of the user as he has chosen in its profile in Silverpeas.
   */
  public String getUserLanguage() {
    String language = I18NHelper.DEFAULT_LANGUAGE;
    User user = User.getCurrentRequester();
    if (user != null) {
      language = user.getUserPreferences().getLanguage();
    }
    return language;
  }

  /**
   * Retrieves from {@link HttpServletRequest} a collection of {@link UploadedFile}.
   * @return collection of {@link UploadedFile}. Empty collection if no uploaded file exists.
   */
  public Collection<UploadedFile> getUploadedFiles() {
    Collection<UploadedFile> uploadedFiles = new ArrayList<>();
    User user = User.getCurrentRequester();
    if (user != null) {
      uploadedFiles = FileUploadManager.getUploadedFiles(this, user);
    }
    return uploadedFiles;
  }

  /**
   * Get a parameter value as a boolean.
   * @param attributeName the name of the attribute.
   * @return the value of the attribute as a boolean.
   */
  public boolean getAttributeAsBoolean(String attributeName) {
    return RequestParameterDecoder.asBoolean(getAttribute(attributeName));
  }

  /**
   * Get a parameter value as a Long.
   * @param attributeName the name of the attribute.
   * @return the value of the attribute as a long.
   */
  public Long getAttributeAsLong(String attributeName) {
    return RequestParameterDecoder.asLong(getAttribute(attributeName));
  }

  /**
   * Returns an array of String objects containing all the values the given request parameter
   * has, or null if the parameter does not exist. The parameters from a multipart/form-data stream
   * are also considered by this method, unlike of the default behavior of the decorated request.
   * <p>
   * If the parameter has a single value, the array has a length of 1.
   * </p>
   * @param name the name of the parameter whose value is requested.
   * @return an array of String objects containing the parameter's values.
   */
  @Override
  public String[] getParameterValues(String name) {
    String[] values = super.getParameterValues(name);
    if (values == null && isContentInMultipart() && isNotSOAPRequest() && isNotCmis()) {
      // in some circumstances (like SOAP) we don't have to consume the request input stream as it
      // later requires to be directly read
      List<String> listOfValues =
          FileUploadUtil.getParameterValues(getFileItems(), name, getCharacterEncoding());
      values = listOfValues.toArray(new String[0]);
    }
    return values;
  }

  /**
   * Returns an Enumeration of String objects containing the names of the parameters contained in
   * this request. If the request has no parameters, the method returns an empty Enumeration. The
   * parameters from a multipart/form-data stream are also considered by this method, unlike of the
   * default behavior of the decorated request.
   * @return an Enumeration of String objects, each String containing the name of a request
   * parameter; or an empty Enumeration if the request has no parameters.
   */
  @Override
  public Enumeration<String> getParameterNames() {
    Enumeration<String> names = super.getParameterNames();
    if (!names.hasMoreElements() && isContentInMultipart() && isNotSOAPRequest() && isNotCmis()) {
      // in some circumstances (like SOAP) we don't have to consume the request input stream as it
      // later requires to be directly read
      List<FileItem> items = getFileItems();
      List<String> itemNames = new ArrayList<>(items.size());
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
   * <p>
   * Request parameters are extra information sent with the request. For HTTP servlets, parameters
   * are contained in the query string or posted form data. The parameters from a
   * multipart/form-data stream are also considered by this method, unlike of the default behavior
   * of the decorated request.
   * </p>
   * @return an immutable java.util.Map containing parameter names as keys and parameter values as
   * map values. The keys in the parameter map are of type String. The values in the parameter map
   * are of type String array.
   */
  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> map = super.getParameterMap();
    if (map.isEmpty() && isContentInMultipart() && isNotSOAPRequest() && isNotCmis()) {
      // in some circumstances (like SOAP) we don't have to consume the request input stream as it
      // later requires to be directly read
      List<FileItem> items = getFileItems();
      map = new HashMap<>(items.size());
      for (FileItem item : items) {
        if (item.isFormField()) {
          String[] value;
          try {
            value = new String[]{item.getString(getCharacterEncoding())};
          } catch (UnsupportedEncodingException ex) {
            SilverLogger.getLogger(this).warn(ex);
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
   * Same as {@link #getParameterMap()} but with a String as value instead of an array of string.
   * @return an immutable java.util.Map containing parameter names as keys and parameter values as
   * map values. The keys in the parameter map are of type String. The values in the parameter map
   * are of type String.
   */
  public Map<String, String> getParameterSimpleMap() {
    return getParameterMap().entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey,
            e -> stream(e.getValue()).findFirst().orElse(EMPTY)));
  }

  /**
   * Merges into given collection of identifiers the selected and unselected identifiers
   * extracted from the current request.
   * <p>
   * Default parameter names are used:
   * </p>
   * <ul>
   * <li>selectedIds: parameter name to retrieve selected identifiers.</li>
   * <li>unselectedIds: parameter name to retrieve unselected identifiers.</li>
   * </ul>
   * @param selectedIds the collection of selected identifiers.
   */
  public void mergeSelectedItemsInto(Collection<String> selectedIds) {
    mergeSelectedItemsInto(selectedIds, "selectedIds", "unselectedIds");
  }

  /**
   * Merges into given collection of identifiers the selected and unselected identifiers
   * extracted from the current request.
   * @param selectedIds the collection of selected identifiers.
   * @param selectedParamName the parameter name of selected identifiers.
   * @param unselectedParamName the parameter name of unselected identifiers.
   */
  public void mergeSelectedItemsInto(Collection<String> selectedIds, String selectedParamName,
      String unselectedParamName) {
    final String[] selected = getParameterMap().get(selectedParamName);
    final String[] unselected = getParameterMap().get(unselectedParamName);
    if (selected != null) {
      stream(selected).flatMap(i -> stream(i.split(","))).forEach(selectedIds::add);
    }
    if (unselected != null) {
      stream(unselected).flatMap(i -> stream(i.split(","))).forEach(selectedIds::remove);
    }
  }

  /**
   * Returns the value of a request parameter as a String, or null if the parameter does not exist.
   * Request parameters are extra information sent with the request. For HTTP servlets, parameters
   * are contained in the query string or posted form data. The parameters from a
   * multipart/form-data stream are also considered by this method, unlike of the default behavior
   * of the decorated request.
   * <p>
   * You should only use this method when you are sure the parameter has only one value. If the
   * parameter might have more than one value, use getParameterValues(java.lang.String).
   * </p>
   * <p>
   * If you use this method with a multivalued parameter, the value returned is equal to the first
   * value in the array returned by getParameterValues.
   * </p>
   * <p>
   * If the parameter data was sent in the request body, such as occurs with an HTTP POST request,
   * then reading the body directly via getInputStream() or getReader() can interfere with the
   * execution of this method.
   * </p>
   * @param name the name of the parameter.
   * @return the single value of the parameter.
   */
  @Override
  public String getParameter(String name) {
    String value = super.getParameter(name);
    if (value == null && isContentInMultipart() && isNotSOAPRequest() && isNotCmis()) {
      // in some circumstances (like SOAP) we don't have to consume the request input stream as it
      // later requires to be directly read
      value = FileUploadUtil.getParameter(getFileItems(), name, null, getCharacterEncoding());
    }
    return value;
  }

  /**
   * Is the specified parameter defined?
   * @param name the name of the parameter.
   * @return true if the value of the parameter isn't null and not empty.
   */
  public boolean isParameterDefined(final String name) {
    return StringUtil.isDefined(super.getParameter(name));
  }

  /**
   * Is the specified parameter not null?
   * @param name the name of the parameter.
   * @return true if the parameter is valued, even if this value is empty.
   */
  public boolean isParameterNotNull(final String name) {
    return super.getParameter(name) != null;
  }

  /**
   * Get a parameter value as a {@link List} of string.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a {@link List} of string.
   */
  public List<String> getParameterAsList(String parameterName) {
    String[] values = getParameterMap().get(parameterName);
    if (values == null) {
      values = new String[0];
    }
    if (values.length == 1) {
      return asList(split(values[0], ","));
    }
    return Arrays.stream(values).collect(Collectors.toList());
  }

  /**
   * Get a parameter value as a {@link RequestFile}.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a {@link RequestFile}.
   */
  public RequestFile getParameterAsRequestFile(String parameterName) {
    RequestFile requestFile = null;
    FileItem fileItem = FileUploadUtil.getFile(getFileItems(), parameterName);
    if (fileItem != null) {
      requestFile = new RequestFile(fileItem);
    }
    return requestFile;
  }

  /**
   * Get a parameter value as a boolean.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a boolean.
   */
  public boolean getParameterAsBoolean(String parameterName) {
    return RequestParameterDecoder.asBoolean(getParameter(parameterName));
  }

  /**
   * Get a parameter value as a list of boolean.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a list of boolean.
   */
  @SuppressWarnings("unused")
  public List<Boolean> getParameterAsBooleanList(String parameterName) {
    return getParameterAsList(parameterName).stream().map(RequestParameterDecoder::asBoolean)
        .collect(Collectors.toList());
  }

  /**
   * Get a parameter value as a Long.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a long.
   */
  public Long getParameterAsLong(String parameterName) {
    return RequestParameterDecoder.asLong(getParameter(parameterName));
  }

  /**
   * Get a parameter value as a list of long.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a list of long.
   */
  @SuppressWarnings("unused")
  public List<Long> getParameterAsLongList(String parameterName) {
    return getParameterAsList(parameterName).stream().map(RequestParameterDecoder::asLong)
        .collect(Collectors.toList());
  }

  /**
   * Get a parameter value as a Integer.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as an integer.
   */
  public Integer getParameterAsInteger(String parameterName) {
    return RequestParameterDecoder.asInteger(getParameter(parameterName));
  }

  /**
   * Get a parameter value as a list of integer.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter as a list of integer.
   */
  @SuppressWarnings("unused")
  public List<Integer> getParameterAsIntegerList(String parameterName) {
    return getParameterAsList(parameterName).stream().map(RequestParameterDecoder::asInteger)
        .collect(Collectors.toList());
  }

  /**
   * Get a date from a date parameter.
   *
   * @param dateParameterName the name of the parameter.
   * @return the value of the parameter as a date.
   * @throws java.text.ParseException if the parameter value isn't a date.
   */
  public Date getParameterAsDate(String dateParameterName) throws ParseException {
    return RequestParameterDecoder.asDate(getParameter(dateParameterName), null, getUserLanguage());
  }

  /**
   * Get a parameter value as a list of date.
   *
   * @param dateParameterName the name of the parameter.
   * @return the value of the parameter as a list of date.
   */
  @SuppressWarnings("unused")
  public List<Date> getParameterAsDateList(String dateParameterName) {
    return getParameterAsList(dateParameterName).stream().map(p -> {
      try {
        return RequestParameterDecoder.asDate(p, null, getUserLanguage());
      } catch (ParseException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }).collect(Collectors.toList());
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
    return RequestParameterDecoder.asDate(getParameter(dateParameterName), getParameter(hourParameterName),
        getUserLanguage());
  }

  /**
   * Get an enum instance from one parameter.
   *
   * @param enumValue the string value of the expected enum instance.
   * @param enumClass the class of the expected enum instance.
   * @param <E> the type of the expected enum instance.
   * @return the expected enum instance or null if enum has not been well decoded
   */
  public <E extends Enum<E>> E getParameterAsEnum(String enumValue, Class<E> enumClass) {
    return RequestParameterDecoder.asEnum(getParameter(enumValue), enumClass);
  }

  /**
   * Get a parameter value as a list of enum.
   *
   * @param parameterName the name of the parameter.
   * @param enumClass the class of the expected enum instance.
   * @return the value of the parameter as a list of enum.
   */
  @SuppressWarnings("unused")
  public <E extends Enum<E>> List<E> getParameterAsEnumList(String parameterName, Class<E> enumClass) {
    return getParameterAsList(parameterName).stream().map(p -> RequestParameterDecoder.asEnum(p, enumClass))
        .collect(Collectors.toList());
  }

  /**
   * Is the content in this request is encoded in a multipart stream.
   * @return true if the content type of this request is a compliant multipart/form-data stream,
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

  private boolean isNotSOAPRequest() {
    return getHeader("SOAPAction") == null && !getContentType().contains("soap");
  }

  private boolean isNotCmis() {
    return !getRequestURI().contains("cmis");
  }
}
