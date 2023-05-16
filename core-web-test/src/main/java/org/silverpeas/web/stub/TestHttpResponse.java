/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
package org.silverpeas.web.stub;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.Pair;

import javax.annotation.Nonnull;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;

/**
 * HTTP request for testing purpose. It allows to create explicitly an {@link HttpServletResponse} from which
 * some expectations can be defined either by customizing its headers or by extending it.
 *
 * @author mmoquillon
 */
public class TestHttpResponse implements HttpServletResponse {

    private final Map<String, List<String>> headers = new HashMap<>();

    private Pair<Integer, String> status;

    @SuppressWarnings("unused")
    @Nonnull
    public TestHttpResponse addHeader(@Nonnull final String name, @Nonnull final String... value) {
        List<String> values = this.headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.addAll(Arrays.asList(value));
        return this;
    }

    @Override
    public void addCookie(Cookie cookie) {
        // nothing
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return URLEncoder.encode(url, Charsets.UTF_8);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return URLEncoder.encode(url, Charsets.UTF_8);
    }

    @Override
    public String encodeUrl(String url) {
        return URLEncoder.encode(url, Charsets.UTF_8);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return URLEncoder.encode(url, Charsets.UTF_8);
    }

    @Override
    public void sendError(int sc, String msg) {
        status = Pair.of(sc, msg);
    }

    @Override
    public void sendError(int sc) {
        status = Pair.of(sc, "");
    }

    @Override
    public void sendRedirect(String location) {
        // nothing
    }

    @Override
    public void setDateHeader(String name, long date) {
        List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.clear();
        values.add(String.valueOf(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(String.valueOf(date));
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.clear();
        values.add(value);
    }

    @Override
    public void addHeader(String name, String value) {
        List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.clear();
        values.add(String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(String.valueOf(value));
    }

    @Override
    public void setStatus(int sc) {
        status = Pair.of(sc, "");
    }

    @Override
    public void setStatus(int sc, String sm) {
        status = Pair.of(sc, sm);
    }

    @Override
    public int getStatus() {
        return status.getFirst();
    }

    @Nonnull
    public Pair<Integer, String> getActualStatus() {
        return status == null ? Pair.of(SC_OK, "") : status;
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headers.getOrDefault(name, List.of());
        return values.isEmpty() ? null : values.get(0);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return new ArrayList<>(headers.getOrDefault(name, List.of()));
    }

    @Override
    public Collection<String> getHeaderNames() {
        return new ArrayList<>(headers.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return null;
    }

    @Override
    public PrintWriter getWriter() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        // nothing
    }

    @Override
    public void setContentLength(int len) {
        // nothing
    }

    @Override
    public void setContentLengthLong(long len) {
        // nothing
    }

    @Override
    public void setContentType(String type) {
        // nothing
    }

    @Override
    public void setBufferSize(int size) {
        // nothing
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() {
        // nothing
    }

    @Override
    public void resetBuffer() {
        // nothing
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
        // nothing
    }

    @Override
    public void setLocale(Locale loc) {
        // nothing
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }
}