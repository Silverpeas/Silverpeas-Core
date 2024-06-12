/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
 */
package org.silverpeas.core.web.filter;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.jcr.webdav.WebDavProtocol;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.security.SecuritySettings;
import org.silverpeas.core.web.SilverpeasWebResource;
import org.silverpeas.core.web.filter.exception.WebSecurityException;
import org.silverpeas.core.web.filter.exception.WebSqlInjectionSecurityException;
import org.silverpeas.core.web.filter.exception.WebXssInjectionSecurityException;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.util.URLUtil.getCurrentServerURL;

/**
 * Massive Web Security Protection.
 * <p>
 * For now, this filter ensures HTTPS is used in secured connections, blocks content sniffing of web
 * browsers, and checks XSS and SQL injections in URLs.
 *
 * @author Yohann Chastagnier
 */
public class MassiveWebSecurityFilter implements Filter {

  private static final SilverLogger logger = SilverLogger.getLogger("silverpeas.core.security");

  private static final String WEB_SERVICES_URI_PREFIX =
      SilverpeasWebResource.getBasePathBuilder().build().toString();

  private static final String WEB_PAGES_URI_PREFIX =
      UriBuilder.fromUri(URLUtil.getApplicationURL()).path("RwebPages").build().toString();

  private static final String CMIS_URI_PREFIX =
      UriBuilder.fromPath(URLUtil.getApplicationURL()).path("cmis").build().toString();

  private static final List<Pattern> SQL_SKIPPED_PARAMETER_PATTERNS;
  private static final List<Pattern> XSS_SKIPPED_PARAMETER_PATTERNS;

  private static final List<Pattern> SQL_PATTERNS;
  private static final List<Pattern> XSS_PATTERNS;

  private static final Pattern ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN =
      Pattern.compile("(?ui)[a-z\\d\\-_éèçàëäüïöâêûîôµù]$");


  private static final Pattern SQL_SELECT_FROM_PATTERN = Pattern.compile("(?i)select.*from");
  private static final Pattern SQL_INSERT_VALUES_PATTERN =
      Pattern.compile("(?i)insert( .*|.* )into.*values");
  private static final Pattern SQL_UPDATE_PATTERN = Pattern.compile("(?i)update.*set");
  private static final Pattern SQL_DELETE_PATTERN = Pattern.compile("(?i)delete( .*|.* )from");
  private static String sqlSelectPatternInspectDeeplyCacheKey = null;


  static {

    // In treatments each sequence of spaces is replaced by one space

    SQL_SKIPPED_PARAMETER_PATTERNS = new ArrayList<>(1);
    if (StringUtil.isDefined(SecuritySettings.skippedParametersAboutWebSqlInjectionSecurity())) {
      SQL_SKIPPED_PARAMETER_PATTERNS.add(
          Pattern.compile(SecuritySettings.skippedParametersAboutWebSqlInjectionSecurity()));
    }
    XSS_SKIPPED_PARAMETER_PATTERNS = new ArrayList<>(1);
    if (StringUtil.isDefined(SecuritySettings.skippedParametersAboutWebXssInjectionSecurity())) {
      XSS_SKIPPED_PARAMETER_PATTERNS.add(
          Pattern.compile(SecuritySettings.skippedParametersAboutWebXssInjectionSecurity()));
    }

    SQL_PATTERNS = new ArrayList<>(6);
    SQL_PATTERNS.add(
        Pattern.compile("(?i)(grant|revoke)(( .*|.* )(select|insert|update|delete))+( .*|.* )on"));
    SQL_PATTERNS.add(
        Pattern.compile("(?i)(grant|revoke)(( .*|.* )(references|alter|index|all))+( .*|.* )on"));
    SQL_PATTERNS.add(Pattern.compile("(?i)(create|drop|alter)( .*|.* )(table|database|schema)"));
    SQL_PATTERNS.add(SQL_SELECT_FROM_PATTERN);
    SQL_PATTERNS.add(SQL_INSERT_VALUES_PATTERN);
    SQL_PATTERNS.add(SQL_UPDATE_PATTERN);
    SQL_PATTERNS.add(SQL_DELETE_PATTERN);

    XSS_PATTERNS = new ArrayList<>(1);
    XSS_PATTERNS.add(Pattern.compile("(?i)<[\\s/]*(script|iframe)"));
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    final HttpRequest httpRequest = new HttpRequestWrapper((HttpRequest) request);
    final HttpServletResponse httpResponse = (HttpServletResponse) response;
    try {
      setDefaultSecurity(httpRequest, httpResponse);
      checkSecurity(httpRequest, httpResponse);

      // The request treatment continues.
      chain.doFilter(httpRequest, httpResponse);

    } catch (WebSecurityException wse) {

      logger.error("The request for path {0} (uid={1}) isn''t valid: {2}", pathOf(httpRequest),
          ofNullable(User.getCurrentRequester()).map(User::getId).orElse("N/A"), wse.getMessage());

      // An HTTP error is sent to the client
      httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, wse.getMessage());
    }
  }

  private void setDefaultSecurity(final HttpServletRequest request,
      final HttpServletResponse response) {
    response.setHeader("X-Content-Type-Options", "nosniff");
    if (request.isSecure() && SecuritySettings.isStrictTransportSecurityEnabled()) {
      response.setHeader("Strict-Transport-Security",
          "max-age=" + SecuritySettings.getStrictTransportSecurityExpirationTime() + "; preload");
    }
  }

  private void checkSecurity(final HttpRequest httpRequest, final HttpServletResponse httpResponse)
      throws WebSqlInjectionSecurityException, WebXssInjectionSecurityException {
    String requestURI = httpRequest.getRequestURI();
    boolean isCmisService = requestURI.startsWith(CMIS_URI_PREFIX);
    boolean isWebServiceMultipart =
        requestURI.startsWith(WEB_SERVICES_URI_PREFIX) && httpRequest.isContentInMultipart();
    boolean isWebPageMultiPart =
        requestURI.startsWith(WEB_PAGES_URI_PREFIX) && httpRequest.isContentInMultipart();

    // Verifying security only if all the prerequisite are satisfied
    if (!(isCmisService || isWebServiceMultipart || isWebPageMultiPart)) {
      checkWebInjection(httpRequest, httpResponse);
      setUpSecurityContentPolicy(httpRequest, httpResponse);
    }
  }

  private void setUpSecurityContentPolicy(final HttpRequest httpRequest,
      final HttpServletResponse httpResponse) {
    boolean isCSPEnabled = SecuritySettings.isWebContentInjectionSecurityEnabled();
    if (isCSPEnabled) {
      final User currentUser = User.getCurrentRequester();
      final String secure = " https: " + (httpRequest.isSecure() ?
          WebDavProtocol.SECURED_WEBDAV_SCHEME + ": " :
          WebDavProtocol.WEBDAV_SCHEME + ": ");
      final String ws = " " + getCurrentServerURL().replaceFirst("^http", "ws") + " ";
      final String font = currentUser == null ? "" : "; font-src * data:";
      final String img = currentUser == null ? "" : "; img-src * data: blob:";
      httpResponse.setHeader("Content-Security-Policy",
          "default-src 'self' blob: mailto: " + secure + ws +
              SecuritySettings.getAllowedDefaultSourcesInCSP() + font + img +
              "; script-src 'self' blob: 'unsafe-inline' 'unsafe-eval' " + secure +
              SecuritySettings.getAllowedScriptSourcesInCSP() +
              "; style-src 'self' 'unsafe-inline' " + secure +
              SecuritySettings.getAllowedStyleSourcesInCSP() +
              "; style-src-elem 'self' blob: 'unsafe-inline' " +
              SecuritySettings.getAllowedStyleSourcesInCSP());
    }
  }

  private void checkWebInjection(final HttpRequest httpRequest,
      final HttpServletResponse httpResponse)
      throws WebSqlInjectionSecurityException, WebXssInjectionSecurityException {
    boolean isWebSqlInjectionSecurityEnabled = SecuritySettings.isWebSqlInjectionSecurityEnabled();
    boolean isWebXssInjectionSecurityEnabled = SecuritySettings.isWebXssInjectionSecurityEnabled();
    if (isWebSqlInjectionSecurityEnabled || isWebXssInjectionSecurityEnabled) {
      if (isWebXssInjectionSecurityEnabled) {
        // this header isn't taken in charge by all web browsers.
        httpResponse.setHeader("X-XSS-Protection", "1");
      }
      checkRequestEntityForInjection(httpRequest);
      checkRequestParametersForInjection(httpRequest, isWebSqlInjectionSecurityEnabled,
          isWebXssInjectionSecurityEnabled);
    }
  }

  private void checkRequestEntityForInjection(final HttpRequest request)
      throws WebSqlInjectionSecurityException, WebXssInjectionSecurityException {
    long start = System.currentTimeMillis();
    try {
      boolean hasSupportedWebEntity = Optional.ofNullable(request.getContentType())
          .map(String::toLowerCase)
          .filter(c -> c.contains("json") || c.contains("xml"))
          .isPresent();
      if (hasSupportedWebEntity) {
        String charset = request.getCharacterEncoding() == null ? "UTF-8" :
            request.getCharacterEncoding();
        InputStream body = request.getInputStream();
        if (body.markSupported()) {
          body.mark(Integer.MAX_VALUE);
          String entity = new String(body.readAllBytes(), charset);
          checkValueForInjection(entity, true, true);
          body.reset();
        }
      }
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    } finally {
      long end = System.currentTimeMillis();
      logger.debug("Massive Web Security Verify on request entity: " +
          DurationFormatUtils.formatDurationHMS(end - start));
    }
  }

  private void checkRequestParametersForInjection(final HttpRequest httpRequest,
      final boolean isWebSqlInjectionSecurityEnabled,
      final boolean isWebXssInjectionSecurityEnabled)
      throws WebSqlInjectionSecurityException, WebXssInjectionSecurityException {
    long start = System.currentTimeMillis();
    try {
      // Browsing all parameters
      for (Map.Entry<String, String[]> parameterEntry : httpRequest.getParameterMap().entrySet()) {

        boolean sqlInjectionToVerify = isWebSqlInjectionSecurityEnabled &&
            mustTheParameterBeVerifiedForSqlVerifications(parameterEntry.getKey());
        boolean xssInjectionToVerify = isWebXssInjectionSecurityEnabled &&
            mustTheParameterBeVerifiedForXssVerifications(parameterEntry.getKey());
        if (!sqlInjectionToVerify && !xssInjectionToVerify) {
          continue;
        }

        checkParameterValues(parameterEntry, sqlInjectionToVerify, xssInjectionToVerify);
      }
    } finally {
      long end = System.currentTimeMillis();
      logger.debug("Massive Web Security Verify on request parameters: " +
          DurationFormatUtils.formatDurationHMS(end - start));
    }
  }

  private void checkParameterValues(final Map.Entry<String, String[]> parameterEntry,
      final boolean sqlInjectionToVerify, final boolean xssInjectionToVerify)
      throws WebSqlInjectionSecurityException, WebXssInjectionSecurityException {
    for (String parameterValue : parameterEntry.getValue()) {
      checkValueForInjection(parameterValue, sqlInjectionToVerify, xssInjectionToVerify);
    }
  }

  private void checkValueForInjection(String value, boolean sqlInjectionToVerify,
      boolean xssInjectionToVerify) throws WebSqlInjectionSecurityException,
      WebXssInjectionSecurityException {
    Matcher patternMatcherFound;
    // Each sequence of spaces is replaced by one space
    value = value.replaceAll("\\s+", " ");

    // SQL injections?
    if (sqlInjectionToVerify && (patternMatcherFound =
        findPatternMatcherFromString(SQL_PATTERNS, value, true)) != null) {

      if (!verifySqlDeeply(patternMatcherFound, value)) {
        patternMatcherFound = null;
      }

      if (patternMatcherFound != null) {
        throw new WebSqlInjectionSecurityException();
      }
    }

    // XSS injections?
    if (xssInjectionToVerify &&
        findPatternMatcherFromString(XSS_PATTERNS, value, false) != null) {
      throw new WebXssInjectionSecurityException();
    }
  }

  /**
   * Verifies deeply a matched SQL string. Indeed, throwing an exception of XSS attack only on SQL
   * detection is not enough. This method tries to detect a known table name from the SQL string.
   *
   * @param matcherFound a pattern matcher
   * @param statement a SQL statement to check
   * @return true of the SQL statement is considered as safe. False otherwise.
   */
  private boolean verifySqlDeeply(final Matcher matcherFound, String statement) {
    boolean isVerified = true;
    if (matcherFound.pattern() == SQL_SELECT_FROM_PATTERN ||
        matcherFound.pattern() == SQL_INSERT_VALUES_PATTERN ||
        matcherFound.pattern() == SQL_UPDATE_PATTERN ||
        matcherFound.pattern() == SQL_DELETE_PATTERN) {
      isVerified = false;
      Pattern tableNamesPattern = getSqlTableNamesPattern();
      Matcher tableNameMatcher = tableNamesPattern.matcher(statement);
      while (tableNameMatcher.find()) {
        isVerified =
            tableNamesPattern.matcher(extractTableNameWholeWord(tableNameMatcher, statement))
                .matches();
        if (isVerified) {
          break;
        }
      }
    }
    return isVerified;
  }

  /**
   * Extracts the whole table name matching the given pattern. Indeed, the matcher can find a table
   * name that is a part of another one.
   *
   * @param matcher a pattern matcher.
   * @param matchedString a SQL statement part
   * @return a whole table name
   */
  private String extractTableNameWholeWord(Matcher matcher, String matchedString) {
    StringBuilder tableName =
        new StringBuilder(matchedString.substring(matcher.start(), matcher.end()));
    int index = matcher.start() - 1;
    while (index >= 0) {
      if (ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN.matcher(
          String.valueOf(matchedString.charAt(index))).matches()) {
        tableName.insert(0, matchedString.charAt(index));
      } else {
        break;
      }
      index--;
    }
    index = matcher.end();
    while (index < matchedString.length()) {
      if (ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN.matcher(
          String.valueOf(matchedString.charAt(index))).matches()) {
        tableName.append(matchedString.charAt(index));
      } else {
        break;
      }
      index++;
    }
    return tableName.toString();
  }

  /**
   * Gets a pattern that permits to check deeply a detected SELECT FROM with known table names. A
   * cache is handled by this method in order to avoid building at every call the same pattern.
   *
   * @return a regexp pattern.
   */
  private synchronized Pattern getSqlTableNamesPattern() {
    Pattern pattern = (sqlSelectPatternInspectDeeplyCacheKey != null) ?
        CacheAccessorProvider.getApplicationCacheAccessor()
            .getCache()
            .get(sqlSelectPatternInspectDeeplyCacheKey, Pattern.class) :
        null;
    if (pattern == null) {
      StringBuilder sbPattern = new StringBuilder("(");
      for (String tableName : DBUtil.getAllTableNames()) {
        if (sbPattern.length() > 1) {
          sbPattern.append("|");
        }
        sbPattern.append(tableName);
      }
      sbPattern.append(")");

      pattern = Pattern.compile("(?i)" + sbPattern);
      sqlSelectPatternInspectDeeplyCacheKey =
          CacheAccessorProvider.getApplicationCacheAccessor().getCache().add(pattern);
    }
    return pattern;
  }

  /**
   * Must the given parameter be skipped from SQL injection verifying?
   *
   * @param parameterName name of a parameter.
   * @return true if the given parameter has to be skipped. False otherwise.
   */
  private boolean mustTheParameterBeVerifiedForSqlVerifications(String parameterName) {
    return findPatternMatcherFromString(SQL_SKIPPED_PARAMETER_PATTERNS, parameterName, false) ==
        null;
  }

  /**
   * Must the given parameter be skipped from XSS injection verifying?
   *
   * @param parameterName name of a parameter.
   * @return true of the given parameter has to be skipped. False otherwise.
   */
  private boolean mustTheParameterBeVerifiedForXssVerifications(String parameterName) {
    return findPatternMatcherFromString(XSS_SKIPPED_PARAMETER_PATTERNS, parameterName, false) ==
        null;
  }

  /**
   * Gets the matcher corresponding to the pattern in the given list of patterns and for which the
   * specified string is compliant.
   *
   * @param patterns a list of pattern to apply on the given string.
   * @param string a string to check.
   * @param startsAndEndsByWholeWord a flag indicating the pattern should match for the first and
   * for the end word in the string.
   * @return the pattern matcher matching the given string.
   */
  private Matcher findPatternMatcherFromString(List<Pattern> patterns, String string,
      boolean startsAndEndsByWholeWord) {
    Matcher isMatcherFound = null;
    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(string);
      if (matcher.find() && !(startsAndEndsByWholeWord &&
          (!verifyMatcherStartingByAWord(matcher, string) ||
              !verifyMatcherEndingByAWord(matcher, string)))) {
        isMatcherFound = matcher;
        break;
      }
    }
    return isMatcherFound;
  }

  /**
   * Verifies that the first word of matching starts with a whole word.
   *
   * @param matcher a matcher.
   * @param matchedString a string.
   * @return true if the first word of matching starts with a whole word
   */
  private boolean verifyMatcherStartingByAWord(Matcher matcher, String matchedString) {
    return matcher.start() == 0 || !ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN.matcher(
        matchedString.substring(0, matcher.start())).find();
  }

  /**
   * Verifies that the first word of matching ends with a whole word.
   *
   * @param matcher a matcher
   * @param matchedString a string
   * @return true if the first word of matching ends with a whole word.
   */
  private boolean verifyMatcherEndingByAWord(Matcher matcher, String matchedString) {
    return matcher.end(0) == matchedString.length() ||
        !ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN.matcher(
            String.valueOf(matchedString.charAt(matcher.end(0)))).find();
  }

  private String pathOf(HttpServletRequest request) {
    return request.getRequestURI();
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    // Nothing to do.
  }

  @Override
  public void destroy() {
    // Nothing to do.
  }

  /**
   * Wrapper of an {@link HttpRequest} to buffer the input stream on its body in order to
   * allow access and back-and-forth navigation within the body content through the input
   * stream.
   */
  private static class HttpRequestWrapper extends HttpRequest {

    private BufferedServletInputStream input;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the {@link HttpServletRequest} to be wrapped.
     * @throws IllegalArgumentException if the request is null
     */
    public HttpRequestWrapper(HttpRequest request) {
      super(request);
    }

    /**
     * Gets the input stream on the content of the request's body. The input stream is buffered and,
     * as such, position in the stream can be marked and hence reset to the last mark (last
     * marked position in the stream).
     * @return a buffered {@link ServletInputStream}.
     * @throws IOException if an error occurs while opening an input stream on the content of the
     * request's body.
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
      if (input == null) {
        input = new BufferedServletInputStream(super.getInputStream());
      }
      return input;
    }

    private static class BufferedServletInputStream extends ServletInputStream {

      private final ServletInputStream inputStream;
      private final BufferedInputStream buffer;

      private BufferedServletInputStream(ServletInputStream inputStream) {
        this.inputStream = inputStream;
        this.buffer = new BufferedInputStream(inputStream);
      }

      @Override
      public boolean isFinished() {
        try {
          return this.buffer.available() == 0;
        } catch (IOException e) {
          return true;
        }
      }

      @Override
      public boolean isReady() {
        return !isFinished();
      }

      @Override
      public void setReadListener(ReadListener readListener) {
        this.inputStream.setReadListener(readListener);
      }

      @Override
      public int read() throws IOException {
        return buffer.read();
      }

      @Override
      public int read(@NonNull byte[] b, int off, int len) throws IOException {
        return buffer.read(b, off, len);
      }

      @Override
      public long skip(long n) throws IOException {
        return buffer.skip(n);
      }

      @Override
      public int available() throws IOException {
        return buffer.available();
      }

      @Override
      public synchronized void mark(int readLimit) {
        buffer.mark(readLimit);
      }

      @Override
      public synchronized void reset() throws IOException {
        buffer.reset();
      }

      @Override
      public boolean markSupported() {
        return buffer.markSupported();
      }

      @Override
      public void close() throws IOException {
        buffer.close();
      }

      @Override
      public int read(@NonNull byte[] b) throws IOException {
        return buffer.read(b);
      }

      @Override
      public byte[] readAllBytes() throws IOException {
        return buffer.readAllBytes();
      }

      @Override
      public byte[] readNBytes(int len) throws IOException {
        return buffer.readNBytes(len);
      }

      @Override
      public int readNBytes(byte[] b, int off, int len) throws IOException {
        return buffer.readNBytes(b, off, len);
      }

      @Override
      public long transferTo(OutputStream out) throws IOException {
        return buffer.transferTo(out);
      }

    }
  }
}
