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
 * You should have received a copy of the GNU Affero General Public Licence
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.web.test.stub;

import javax.annotation.Nonnull;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HTTP request for testing purpose. It allows to create explicitly an {@link HttpServletRequest} from which
 * some expectations can be defined either by customizing its headers or by extending it.
 * @author mmoquillon
 */
public class TestHttpRequest implements HttpServletRequest {

    private final Map<String, List<String>> headers = new HashMap<>();
    private final Map<String, List<String>> parameters = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    private final String uri;
    private final String method;

    public TestHttpRequest(@Nonnull final String method, @Nonnull final String uri) {
        this.method = method;
        this.uri = uri;
    }

    @SuppressWarnings("unused")
    @Nonnull
    public TestHttpRequest addHeader(@Nonnull final String name, @Nonnull final String... value) {
        List<String> values = this.headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.addAll(Arrays.asList(value));
        return this;
    }

    @SuppressWarnings("unused")
    @Nonnull
    public TestHttpRequest addParameter(@Nonnull final String name, @Nonnull final String value) {
        List<String> values = this.parameters.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(value);
        return this;
    }

    @Override
    public String getAuthType() {
        return "BASIC_AUTH";
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        List<String> values = headers.getOrDefault(name, List.of());
        return values.size() == 1 ? Long.parseLong(values.get(0)) : -1L;
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headers.getOrDefault(name, List.of());
        return values.isEmpty() ? null : values.get(0);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return new StringEnumeration(headers.getOrDefault(name, List.of()));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return new StringEnumeration(headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        List<String> values = headers.getOrDefault(name, List.of());
        return values.size() == 1 ? Integer.parseInt(values.get(0)) : -1;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        int start = uri.indexOf("/");
        int end  = uri.contains("?") ? uri.indexOf("?") : uri.length();
        return start > -1  ? uri.substring(start, end) : uri;
    }

    @Override
    public String getQueryString() {
        int start = uri.indexOf("?");
        return start > -1 ? uri.substring(start + 1) : null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(uri);
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) {
        return false;
    }

    @Override
    public void login(String username, String password) {
        // nothing
    }

    @Override
    public void logout() {
        // nothing
    }

    @Override
    public Collection<Part> getParts() {
        return List.of();
    }

    @Override
    public Part getPart(String name) {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new StringEnumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) {
        // nothing
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() {
        return null;
    }

    @Override
    public String getParameter(String name) {
        List<String> values = parameters.getOrDefault(name, List.of());
        return values.isEmpty()?  null : values.get(0);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new StringEnumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        List<String> values = parameters.getOrDefault(name, List.of());
        return values.isEmpty() ? null : values.toArray(new String[0]);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toArray(new String[0])));
    }

    @Override
    public String getProtocol() {
        return "HTTP/2.0";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return "localhost";
    }

    @Override
    public int getServerPort() {
        return 8000;
    }

    @Override
    public BufferedReader getReader() {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return "127.0.0.1";
    }

    @Override
    public String getRemoteHost() {
        return "localhost";
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.attributes.put(name, o);

    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    private static class StringEnumeration implements Enumeration<String> {

        private final ArrayList<String> items;

        private int cursor = -1;

        public StringEnumeration(final Collection<String> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        public boolean hasMoreElements() {
            return ++cursor < items.size();
        }

        @Override
        public String nextElement() {
            return items.get(cursor);
        }
    }
}