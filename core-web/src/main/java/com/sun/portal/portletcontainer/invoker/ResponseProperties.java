/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.invoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;

import org.w3c.dom.Element;

/**
 * ResponseProperties holds the properties set by the Portlet. These properties include cookies and
 * headers that are set on the Response. It also includes DOM element should be added to the markup
 * head section of the response to the client.
 */
public class ResponseProperties {

  private final Map<String, List<String>> responseHeaders;
  private final Map<String, List<Element>> markupHeaders;
  private final List<Cookie> cookies;

  public ResponseProperties() {
    responseHeaders = new HashMap<>();
    markupHeaders = new HashMap<>();
    cookies = new ArrayList<>();
  }

  /**
   * Returns the Map of response headers that are set are by the portlet using addProperty method of
   * PortletResponse. If there is no response headers, returns an empty map.
   * @return the Map of the response headers that set by the portlet
   */
  public Map<String, List<String>> getResponseHeaders() {
    return responseHeaders;
  }

  /**
   * Returns the list of DOM Elements that are set by the portlet using addProperty method of
   * PortletResponse with the property name as "javax.portlet.markup.head.element". If there is no
   * DOM elements, it returns an empty list.
   * @return the list of the DOM Elements that set by the portlet
   */
  public List<Element> getMarkupHeadElements() {
    List<Element> markupHeadElements;
    markupHeadElements = markupHeaders.get("javax.portlet.markup.head.element");
    return markupHeadElements == null ? Collections.emptyList() : markupHeadElements;
  }

  /**
   * Returns the list of Cookies that are set by the portlet using addProperty method of
   * PortletResponse. If there is no cookies, it returns an empty list.
   * @return the list of the DOM Elements that set by the portlet
   */
  public List<Cookie> getCookies() {
    return cookies;
  }

  /**
   * Sets the response headers.
   * @param responseHeaders the response headers
   */
  public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
    if (responseHeaders != null) {
      this.responseHeaders.putAll(responseHeaders);
    }
  }

  /**
   * Sets the markup headers.
   * @param markupHeaders the markup headers
   */
  public void setMarkupHeaders(Map<String, List<Element>> markupHeaders) {
    if (markupHeaders != null) {
      this.markupHeaders.putAll(markupHeaders);
    }
  }

  /**
   * Sets the cookies.
   * @param cookies the cookies
   */
  public void setCookies(List<Cookie> cookies) {
    if (cookies != null) {
      this.cookies.addAll(cookies);
    }
  }

  /**
   * Clears the response properties
   */
  public void clear() {
    this.responseHeaders.clear();
    this.markupHeaders.clear();
    this.cookies.clear();
  }
}
