/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sun.portal.portletcontainer.invoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.w3c.dom.Element;

/**
 * ResponseProperties holds the properties set by the Portlet. These properties
 * include cookies and headers that are set on the Response. It also includes
 * DOM element should be added to the markup head section of the response to the client.
 * 
 */
public class ResponseProperties {

    private Map<String, List<String>> responseHeaders;
    private Map<String, List<Element>> markupHeaders;
    private List<Cookie> cookies;

    public ResponseProperties() {
        responseHeaders = new HashMap<String, List<String>>();
        markupHeaders = new HashMap<String, List<Element>>();
        cookies = new ArrayList<Cookie>();
    }

    /**
     * Returns the Map of response headers that are set are by the portlet using addProperty method
     * of PortletResponse. If there is no response headers, returns and empty map.
     * 
     * @return the Map of the response headers that set by the portlet
     */
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders == null ? Collections.EMPTY_MAP : responseHeaders;
    }

    /**
     * Returns the list of DOM Elements that are set by the portlet using addProperty method
     * of PortletResponse with the property name as "javax.portlet.markup.head.element".
     * If there is no DOM elements, it returns an empty list.
     * 
     * @return the list of the DOM Elements that set by the portlet
     */
    public List<Element> getMarkupHeadElements() {
        List<Element> markupHeadElements = null;
        if(markupHeaders != null) {
            markupHeadElements = markupHeaders.get("javax.portlet.markup.head.element");
        }
        return markupHeadElements == null ? Collections.EMPTY_LIST : markupHeadElements;
    }

    /**
     * Returns the list of Cookies that are set by the portlet using addProperty method
     * of PortletResponse.
     * If there is no cookies, it returns an empty list.
     * 
     * @return the list of the DOM Elements that set by the portlet
     */
    public List<Cookie> getCookies() {
        return cookies == null ? Collections.EMPTY_LIST : cookies;
    }

    /**
     * Sets the response headers.
     * 
     * @param responseHeaders the response headers
     */
    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        if (responseHeaders != null) {
            this.responseHeaders.putAll(responseHeaders);
        }
    }

    /**
     * Sets the markup headers.
     * 
     * @param markupHeaders the markup headers
     */
    public void setMarkupHeaders(Map<String, List<Element>> markupHeaders) {
        if (markupHeaders != null) {
            this.markupHeaders.putAll(markupHeaders);
        }
    }

    /**
     * Sets the cookies.
     * 
     * @param cookies the cookies
     */
    public void setCookies(List<Cookie> cookies) {
        if (cookies != null) {
            this.cookies.addAll(cookies);
        }
    }

    /**
     *  Clears the response properties
     */
    public void clear() {
        this.responseHeaders.clear();
        this.markupHeaders.clear();
        this.cookies.clear();
    }
}
