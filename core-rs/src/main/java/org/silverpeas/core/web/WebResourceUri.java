/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.web;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * URI of the current web resource, that is the web resource being in execution and whose execution
 * was triggered by an incoming HTTP request; the request URI. It is defined
 * to replace the use of {@link UriInfo} as it takes care of the Silverpeas settings in regard to
 * the URL, host and web context definitions and as it is dedicated to compute more easily the URI
 * of the Web entities involved by the incoming HTTP requests.
 * <p>
 * The different properties of such a running web resource URI are explained by an example here.
 * For the following request URI:
 * </p>
 * <pre>{@code http://localhost:8000/silverpeas/services/toto/kmelia32/content/bidule?p=3&u=10}</pre>
 * <ul>
 *   <li>The base URI is {@code http://localhost:8000/silverpeas/services}</li>
 *   <li>The request URI is {@code http://localhost:8000/silverpeas/services/toto/kmelia32/content/bidule?p=3&u=10}</li>
 *   <li>The absolute web resource path is {@code http://localhost:8000/silverpeas/services/toto/kmelia32}</li>
 *   <li>The web resource path is {@code /silverpeas/services/toto/kmelia32}</li>
 *   <li>The absolute path is {@code http://localhost:8000/silverpeas/services/toto/kmelia32/content/bidule}</li>
 *   <li>The path is {@code /silverpeas/services/toto/kmelia32/content/bidule}</li>
 *   <li>The query parameters are taken from the parsing of {@code ?p=3&u=10}</li>
 *   <li>The path parameters are all nodes in the URI that matches with an parameter in the
 *   @{@link javax.ws.javax.ws.rs.Path} annotations of the web resource</li>
 * </ul>
 * @author mmoquillon
 */
public class WebResourceUri {

  private final String webResourcePath;
  private final HttpServletRequest request;
  private final UriInfo uriInfo;

  public WebResourceUri(final String webResourcePath, final HttpServletRequest request,
      final UriInfo uriInfo) {
    this.webResourcePath = webResourcePath;
    this.request = request;
    this.uriInfo = uriInfo;
  }

  public WebResourceUri(final String webResourcePath, final HttpServletRequest request) {
    this(webResourcePath, request, null);
  }

  /**
   * Gets the current HTTP request targeting this URI.
   * @return the current HTTP request
   */
  public HttpServletRequest getHttpRequest() {
    return request;
  }

  /**
   * Gets the base path of the current web resource relative to the root path of the Silverpeas URI.
   * If the web resource targeted by the current request is related to a given component instance,
   * then the identifier of the component instance is also referred in the returned path.
   * All sequences of escaped octets are decoded.
   * @return the base path of the requested web resource.
   */
  public URI getWebResourcePath() {
    return SilverpeasWebResource.getBasePathBuilder().path(webResourcePath).build();
  }

  /**
   * Gets the path of the current web resource relative to the root path of the Silverpeas URI in
   * the form of a {@link UriBuilder}. If the web resource targeted by the current request is
   * related to a given component instance, then the identifier of the component instance is also
   * referred in the returned path.
   * <p>
   * The returned {@link UriBuilder} gives possibility to enrich the path to build a customized URI
   * for a web entity.
   * </p>
   * @return the {@link UriBuilder} initialized with the base path of the requested web resource.
   */
  public UriBuilder getWebResourcePathBuilder() {
    return SilverpeasWebResource.getBasePathBuilder().path(webResourcePath);
  }

  /**
   * Gets the absolute base path of the current web resource. This includes everything preceding the
   * path (host, port, etc).
   * If the web resource targeted by the current request is related to a given component instance,
   * then the identifier of the component instance is also referred in the returned path.
   * All sequences of escaped octets are decoded.
   * @return the absolute base path of the requested web resource.
   */
  public URI getAbsoluteWebResourcePath() {
    return SilverpeasWebResource.getBaseUriBuilder(request).path(webResourcePath).build();
  }

  /**
   * Gets the absolute path of the current web resource in the form of a {@link UriBuilder}.
   * This includes everything preceding the path (host, port, etc). If the web resource targeted by
   * the current request is related to a given component instance, then the identifier of the
   * component instance is also referred in the returned path.
   * <p>
   * The returned {@link UriBuilder} gives possibility to enrich the path to build a customized URI
   * for a web entity.
   * </p>
   * @return the {@link UriBuilder} initialized with the absolute base path of the requested web
   * resource.
   */
  public UriBuilder getAbsoluteWebResourcePathBuilder() {
    return SilverpeasWebResource.getBaseUriBuilder(request).path(webResourcePath);
  }

  /**
   * Gets the path of the current request relative to the root path of the Silverpeas URI.
   * All sequences of escaped octets are decoded.
   * @return the path of the requested URI.
   */
  public URI getPath() {
    return getPathBuilder().build();
  }

  /**
   * Gets the path of the current request relative to the root path of the Silverpeas URI in the
   * form of a {@link UriBuilder}.
   * <p>
   * The returned {@link UriBuilder} gives possibility to enrich the path to build a customized URI
   * for a web entity.
   * </p>
   * @return the {@link UriBuilder} initialized with the path of the requested URI.
   */
  public UriBuilder getPathBuilder() {
    return SilverpeasWebResource.getBasePathBuilder().path(request.getPathInfo());
  }

  /**
   * Get the absolute request URI including any query parameters. It takes into account the
   * Silverpeas settings about the application host and context definition.
   * @return the absolute request URI.
   */
  public URI getRequestUri() {
    return getRequestUriBuilder().build();
  }

  /**
   * Get the absolute request URI including any query parameters in the form of a {@link
   * UriBuilder}. It takes into account the Silverpeas settings about the application host and
   * context definition.
   * <p>
   * The returned {@link UriBuilder} gives possibility to enrich the path to build a customized URI
   * for a web entity.
   * </p>
   * @return the {@link UriBuilder} initialized with the absolute request URI.
   */
  public UriBuilder getRequestUriBuilder() {
    UriBuilder uri = SilverpeasWebResource.getBaseUriBuilder(request).path(uriInfo.getPath());
    uriInfo.getQueryParameters()
        .forEach((q, v) -> uri.queryParam(q, v.toArray(new Object[0])));
    return uri;
  }

  /**
   * Get the absolute path of the request. This includes everything preceding the path (host, port
   * etc) but excludes query parameters.
   * @return the absolute path of the request.
   */
  public URI getAbsolutePath() {
    return getAbsolutePathBuilder().build();
  }

  /**
   * Get the absolute path of the request in the form of a {@link UriBuilder}. This includes
   * everything preceding the path (host, port, etc) but excludes query parameters.
   * <p>
   * The returned {@link UriBuilder} gives possibility to enrich the path to build a customized URI
   * for a web entity.
   * </p>
   * @return the {@link UriBuilder} initialized with the absolute path of the request.
   */
  public UriBuilder getAbsolutePathBuilder() {
    return SilverpeasWebResource.getBaseUriBuilder(request).path(request.getPathInfo());
  }

  /**
   * Get the base URI of all Web resources in Silverpeas. URIs of REST web services are all relative
   * to this base URI.
   * @return the base URI of all of the web resources in Silverpeas (REST web services).
   */
  public URI getBaseUri() {
    return getBaseUriBuilder().build();
  }

  /**
   * Get the base URI of all Web resources in Silverpeas in the form of a {@link UriBuilder}.
   * URIs of root REST web services are all relative to this base URI.
   * @return the {@link UriBuilder} initialized with the base URI of all of the web resources in
   * Silverpeas (REST web services) as an URI builder.
   */
  public UriBuilder getBaseUriBuilder() {
    return SilverpeasWebResource.getBaseUriBuilder(request);
  }

  /**
   * Get the values of any embedded URI template parameters. All sequences of escaped octets are
   * decoded.
   * @return an unmodifiable map of parameter names and values.
   */
  public MultivaluedMap<String, String> getPathParameters() {
    return uriInfo.getPathParameters();
  }

  /**
   * Get the URI query parameters of the current request. The map keys are the names of the query
   * parameters with any escaped characters decoded. All sequences of escaped octets in parameter
   * names and values are decoded.
   * @return an unmodifiable map of query parameter names and values.
   */
  public MultivaluedMap<String, String> getQueryParameters() {
    return uriInfo.getQueryParameters();
  }
}
  