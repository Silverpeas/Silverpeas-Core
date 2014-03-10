/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.web.filter;

import com.stratelia.webactiv.util.DBUtil;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.util.security.SecuritySettings;
import org.silverpeas.web.filter.exception.WebSecurityException;
import org.silverpeas.web.filter.exception.WebSqlInjectionSecurityException;
import org.silverpeas.web.filter.exception.WebXssInjectionSecurityException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For now, this filter verifies XSS and SQL injections in URLs.
 * User: Yohann Chastagnier
 * Date: 05/03/14
 */
public class MassiveWebSecurityFilter implements Filter {

  private static final Logger logger = Logger.getLogger(MassiveWebSecurityFilter.class.
      getSimpleName());

  private final static List<Pattern> SKIPED_PARAMETER_PATTERNS;

  private final static List<Pattern> SQL_PATTERNS;
  private final static List<Pattern> XSS_PATTERNS;

  private final static Pattern ENDS_WITH_WORD_CHARACTER_OR_NUMERIC_PATTERN =
      Pattern.compile("(?i)[\\w\\-_éèçàëäüïöâêûîôµùÉÈÇÀËÄÜÏÖÂÊÛÎÔΜÙ]$");


  private final static Pattern SQL_SELECT_FROM_PATTERN = Pattern.compile("(?i)select.*from");
  private final static Pattern SQL_INSERT_VALUES_PATTERN =
      Pattern.compile("(?i)insert( .*|.* )into.*values");
  private final static Pattern SQL_UPDATE_PATTERN = Pattern.compile("(?i)update.*set");
  private final static Pattern SQL_DELETE_PATTERN = Pattern.compile("(?i)delete( .*|.* )from");
  private static String SQL_SELECT_PATTERN_INSPECT_DEEPLY_CACHE_KEY = null;


  static {

    // In treatments each sequence of spaces is replaced by one space

    SKIPED_PARAMETER_PATTERNS = new ArrayList<Pattern>(1);
    SKIPED_PARAMETER_PATTERNS.add(Pattern.compile("(?i)^(editor|sqlreq)"));

    SQL_PATTERNS = new ArrayList<Pattern>(6);
    SQL_PATTERNS.add(Pattern.compile("(?i)(grant|revoke)" +
        "(( .*|.* )(select|insert|update|delete|references|alter|index|all))+( .*|.* )on"));
    SQL_PATTERNS.add(Pattern.compile("(?i)(create|drop|alter)( .*|.* )(table|database|schema)"));
    SQL_PATTERNS.add(SQL_SELECT_FROM_PATTERN);
    SQL_PATTERNS.add(SQL_INSERT_VALUES_PATTERN);
    SQL_PATTERNS.add(SQL_UPDATE_PATTERN);
    SQL_PATTERNS.add(SQL_DELETE_PATTERN);

    XSS_PATTERNS = new ArrayList<Pattern>(1);
    XSS_PATTERNS.add(Pattern.compile("(?i)<[\\s/]*script"));
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpServletResponse httpResponse = (HttpServletResponse) response;

    try {

      boolean isWebSqlInjectionSecurityEnabled =
          SecuritySettings.isWebSqlInjectionSecurityEnabled();
      boolean isWebXssInjectionSecurityEnabled =
          SecuritySettings.isWebXssInjectionSecurityEnabled();

      // Verifying security only if one is at least enabled
      if (isWebSqlInjectionSecurityEnabled || isWebXssInjectionSecurityEnabled) {

        long start = System.currentTimeMillis();
        try {

          // Browsing all parameters
          Matcher patternMatcherFound;
          for (Map.Entry<String, String[]> parameterEntry : httpRequest.getParameterMap()
              .entrySet()) {

            if (!mustTheParameterBeVerified(parameterEntry.getKey())) {
              continue;
            }

            for (String parameterValue : parameterEntry.getValue()) {

              // Each sequence of spaces is replaced by one space
              parameterValue = parameterValue.replaceAll("\\s+", " ");

              // SQL injections?
              if (isWebSqlInjectionSecurityEnabled && (patternMatcherFound =
                  findPatternMatcherFromString(SQL_PATTERNS, parameterValue, true)) != null) {

                if (!verifySqlDeeply(patternMatcherFound, parameterValue)) {
                  patternMatcherFound = null;
                }

                if (patternMatcherFound != null) {
                  throw new WebSqlInjectionSecurityException();
                }
              }

              // XSS injections?
              if (isWebXssInjectionSecurityEnabled &&
                  findPatternMatcherFromString(XSS_PATTERNS, parameterValue, false) != null) {
                throw new WebXssInjectionSecurityException();
              }
            }
          }
        } finally {
          long end = System.currentTimeMillis();
          if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Massive Web Security Verify duration : " +
                DurationFormatUtils.formatDurationHMS(end - start));
          }
        }
      }

      // The request treatment continues.
      chain.doFilter(httpRequest, httpResponse);

    } catch (WebSecurityException wse) {

      logger.log(Level.SEVERE, "The request for path {0} isn''t valid: {1}",
          new String[]{pathOf(httpRequest), wse.getMessage()});

      // An HTTP error is sended to the client
      httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, wse.getMessage());
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
    Pattern pattern = (SQL_SELECT_PATTERN_INSPECT_DEEPLY_CACHE_KEY != null) ?
        CacheServiceFactory.getApplicationCacheService()
            .get(SQL_SELECT_PATTERN_INSPECT_DEEPLY_CACHE_KEY, Pattern.class) : null;
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
      SQL_SELECT_PATTERN_INSPECT_DEEPLY_CACHE_KEY =
          CacheServiceFactory.getApplicationCacheService().add(pattern);
    }
    return pattern;
  }

  /**
   * Must the given paramater be skiped from injection verifying?
   * @param parameterName
   * @return
   */
  private boolean mustTheParameterBeVerified(String parameterName) {
    return findPatternMatcherFromString(SKIPED_PARAMETER_PATTERNS, parameterName, false) == null;
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
      if (matcher.find()) {
        if (startsAndEndsByWholeWord && (!verifyMatcherStartingByAWord(matcher, string) ||
            !verifyMatcherEndingByAWord(matcher, string))) {
          continue;
        }
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
