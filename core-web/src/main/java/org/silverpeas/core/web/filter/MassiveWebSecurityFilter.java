/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.filter;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.SilverpeasWebResource;
import org.silverpeas.core.web.attachment.WebDavProtocol;
import org.silverpeas.core.web.filter.exception.WebSecurityException;
import org.silverpeas.core.web.filter.exception.WebSqlInjectionSecurityException;
import org.silverpeas.core.web.filter.exception.WebXssInjectionSecurityException;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.util.security.SecuritySettings;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Massive Web Security Protection.
 *
 * For now, this filter ensures HTTPS is used in secured connections, blocks content sniffing of
 * web browsers, and checks XSS and SQL injections
 * in URLs.
 * @author Yohann Chastagnier
 */
public class MassiveWebSecurityFilter implements Filter {

  private static final SilverLogger logger = SilverLogger.getLogger("silverpeas.core.security");

  private static final String WEB_SERVICES_URI_PREFIX =
      SilverpeasWebResource.getBasePathBuilder().build().toString();

  private static final String WEB_PAGES_URI_PREFIX =
      UriBuilder.fromUri(URLUtil.getApplicationURL()).path("RwebPages").build().toString();

  private static final List<Pattern> SQL_SKIPPED_PARAMETER_PATTERNS;
  private static final List<Pattern> XSS_SKIPPED_PARAMETER_PATTERNS;

  private static final List<Pattern> SQL_PATTERNS;
  private static final List<Pattern> XSS_PATTERNS;

  private static final Pattern ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN =
      Pattern.compile("(?i)[\\w\\-_éèçàëäüïöâêûîôµùÉÈÇÀËÄÜÏÖÂÊÛÎÔΜÙ]$");


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
      SQL_SKIPPED_PARAMETER_PATTERNS
          .add(Pattern.compile(SecuritySettings.skippedParametersAboutWebSqlInjectionSecurity()));
    }
    XSS_SKIPPED_PARAMETER_PATTERNS = new ArrayList<>(1);
    if (StringUtil.isDefined(SecuritySettings.skippedParametersAboutWebXssInjectionSecurity())) {
      XSS_SKIPPED_PARAMETER_PATTERNS
          .add(Pattern.compile(SecuritySettings.skippedParametersAboutWebXssInjectionSecurity()));
    }

    SQL_PATTERNS = new ArrayList<>(6);
    SQL_PATTERNS.add(Pattern.compile("(?i)(grant|revoke)" +
        "(( .*|.* )(select|insert|update|delete|references|alter|index|all))+( .*|.* )on"));
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
    final HttpRequest httpRequest = (HttpRequest) request;
    final HttpServletResponse httpResponse = (HttpServletResponse) response;
    try {
      setDefaultSecurity(httpRequest, httpResponse);
      checkSecurity(httpRequest, httpResponse);

      // The request treatment continues.
      chain.doFilter(httpRequest, httpResponse);

    } catch (WebSecurityException wse) {

      logger.error("The request for path {0} isn''t valid: {1}",
          pathOf(httpRequest), wse.getMessage());

      // An HTTP error is sended to the client
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
    boolean isWebServiceMultipart =
        httpRequest.getRequestURI().startsWith(WEB_SERVICES_URI_PREFIX) &&
            httpRequest.isContentInMultipart();
    boolean isWebPageMultiPart = httpRequest.getRequestURI().startsWith(WEB_PAGES_URI_PREFIX) &&
        httpRequest.isContentInMultipart();
    boolean isWebSqlInjectionSecurityEnabled = SecuritySettings.isWebSqlInjectionSecurityEnabled();
    boolean isWebXssInjectionSecurityEnabled = SecuritySettings.isWebXssInjectionSecurityEnabled();
    boolean isCTPEnabled = SecuritySettings.isWebContentInjectionSecurityEnabled();

    // Verifying security only if all the prerequisite are satisfied
    if (!(isWebServiceMultipart || isWebPageMultiPart)) {
      if (isWebSqlInjectionSecurityEnabled || isWebXssInjectionSecurityEnabled) {
        if (isWebXssInjectionSecurityEnabled) {
          // this header isn't taken in charge by all web browsers.
          httpResponse.setHeader("X-XSS-Protection", "1");
        }
        checkRequestParametersForInjection(httpRequest, isWebSqlInjectionSecurityEnabled,
            isWebXssInjectionSecurityEnabled);
      }
      if (isCTPEnabled) {
        final User currentUser = User.getCurrentRequester();
        final String secure = httpRequest.isSecure()
                ? " https: " + WebDavProtocol.SECURED_WEBDAV_SCHEME + ": "
                : WebDavProtocol.WEBDAV_SCHEME + ": ";
        final String img = currentUser == null ? "" : "; img-src * data:";
        httpResponse.setHeader("Content-Security-Policy",
            "default-src 'self' blob: " + secure +
                img +
                "; script-src 'self' blob:  'unsafe-inline' 'unsafe-eval' " + secure +
                SecuritySettings.getAllowedScriptSourcesInCSP() +
                "; style-src 'self' 'unsafe-inline' " + secure +
                SecuritySettings.getAllowedStyleSourcesInCSP());
      }
    }
  }

  private void checkRequestParametersForInjection(final HttpRequest httpRequest,
      final boolean isWebSqlInjectionSecurityEnabled,
      final boolean isWebXssInjectionSecurityEnabled)
      throws WebSqlInjectionSecurityException, WebXssInjectionSecurityException {
    long start = System.currentTimeMillis();
    try {
      // Browsing all parameters
      for (Map.Entry<String, String[]> parameterEntry : httpRequest.getParameterMap()
          .entrySet()) {

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
      logger.debug("Massive Web Security Verify duration : " +
          DurationFormatUtils.formatDurationHMS(end - start));
    }
  }

  private void checkParameterValues(final Map.Entry<String, String[]> parameterEntry,
      final boolean sqlInjectionToVerify, final boolean xssInjectionToVerify)
      throws WebSqlInjectionSecurityException, WebXssInjectionSecurityException {
    Matcher patternMatcherFound;
    for (String parameterValue : parameterEntry.getValue()) {

      // Each sequence of spaces is replaced by one space
      parameterValue = parameterValue.replaceAll("\\s+", " ");

      // SQL injections?
      if (sqlInjectionToVerify && (patternMatcherFound =
          findPatternMatcherFromString(SQL_PATTERNS, parameterValue, true)) != null) {

        if (!verifySqlDeeply(patternMatcherFound, parameterValue)) {
          patternMatcherFound = null;
        }

        if (patternMatcherFound != null) {
          throw new WebSqlInjectionSecurityException();
        }
      }

      // XSS injections?
      if (xssInjectionToVerify &&
          findPatternMatcherFromString(XSS_PATTERNS, parameterValue, false) != null) {
        throw new WebXssInjectionSecurityException();
      }
    }
  }

  /**
   * Verifies deeply a matched SQL string. Indeed, throwing an exception of XSS attack only on
   * SQL detection is not enough. This method tries to detect a known table name from the SQL
   * string.
   * @param matcherFound
   * @param string
   * @return
   */
  private boolean verifySqlDeeply(final Matcher matcherFound, String string) {
    boolean isVerified = true;
    if (matcherFound.pattern() == SQL_SELECT_FROM_PATTERN ||
        matcherFound.pattern() == SQL_INSERT_VALUES_PATTERN ||
        matcherFound.pattern() == SQL_UPDATE_PATTERN ||
        matcherFound.pattern() == SQL_DELETE_PATTERN) {
      isVerified = false;
      Pattern tableNamesPattern = getSqlTableNamesPattern();
      Matcher tableNameMatcher = tableNamesPattern.matcher(string);
      while (tableNameMatcher.find()) {
        isVerified = tableNamesPattern.matcher(extractTableNameWholeWord(tableNameMatcher, string))
            .matches();
        if (isVerified) {
          break;
        }
      }
    }
    return isVerified;
  }

  /**
   * Extracts the whole table name matched. Indeed, the matcher can find a table name that is a part
   * of another ...
   * @param matcher
   * @param matchedString
   * @return
   */
  private String extractTableNameWholeWord(Matcher matcher, String matchedString) {
    StringBuilder tableName =
        new StringBuilder(matchedString.substring(matcher.start(), matcher.end()));
    int index = matcher.start() - 1;
    while (index >= 0) {
      if (ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN
          .matcher(String.valueOf(matchedString.charAt(index))).matches()) {
        tableName.insert(0, matchedString.charAt(index));
      } else {
        break;
      }
      index--;
    }
    index = matcher.end();
    while (index < matchedString.length()) {
      if (ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN
          .matcher(String.valueOf(matchedString.charAt(index))).matches()) {
        tableName.append(matchedString.charAt(index));
      } else {
        break;
      }
      index++;
    }
    return tableName.toString();
  }

  /**
   * Gets a pattern that permits to chexk deeply a detected SELECT FROM with known table names.
   * A cache is handled by this method in order to avoid building at every call the same pattern.
   * @return
   */
  private synchronized Pattern getSqlTableNamesPattern() {
    Pattern pattern = (sqlSelectPatternInspectDeeplyCacheKey != null) ?
        CacheServiceProvider.getApplicationCacheService().getCache()
            .get(sqlSelectPatternInspectDeeplyCacheKey, Pattern.class) : null;
    if (pattern == null) {
      StringBuilder sbPattern = new StringBuilder("(");
      for (String tableName : DBUtil.getAllTableNames()) {
        if (sbPattern.length() > 1) {
          sbPattern.append("|");
        }
        sbPattern.append(tableName);
      }
      sbPattern.append(")");

      pattern = Pattern.compile("(?i)" + sbPattern.toString());
      sqlSelectPatternInspectDeeplyCacheKey =
          CacheServiceProvider.getApplicationCacheService().getCache().add(pattern);
    }
    return pattern;
  }

  /**
   * Must the given parameter be skipped from SQL injection verifying?
   * @param parameterName
   * @return
   */
  private boolean mustTheParameterBeVerifiedForSqlVerifications(String parameterName) {
    return findPatternMatcherFromString(SQL_SKIPPED_PARAMETER_PATTERNS, parameterName, false) ==
        null;
  }

  /**
   * Must the given parameter be skipped from XSS injection verifying?
   * @param parameterName
   * @return
   */
  private boolean mustTheParameterBeVerifiedForXssVerifications(String parameterName) {
    return findPatternMatcherFromString(XSS_SKIPPED_PARAMETER_PATTERNS, parameterName, false) ==
        null;
  }

  /**
   * Indicates the index of one pattern from the given pattern list matches with the given string.
   * @param patterns
   * @param string
   * @param startsAndEndsByWholeWord
   * @return
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
   * @param matcher
   * @param matchedString
   * @return
   */
  private boolean verifyMatcherStartingByAWord(Matcher matcher, String matchedString) {
    return matcher.start() == 0 || !ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN
        .matcher(matchedString.substring(0, matcher.start())).find();
  }

  /**
   * Verifies that the first word of matching starts with a whole word.
   * @param matcher
   * @param matchedString
   * @return
   */
  private boolean verifyMatcherEndingByAWord(Matcher matcher, String matchedString) {
    return matcher.end(0) == matchedString.length() || !ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN
        .matcher(String.valueOf(matchedString.charAt(matcher.end(0)))).find();
  }

  /**
   * Just for code reading.
   * @param request
   * @return
   */
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
}
