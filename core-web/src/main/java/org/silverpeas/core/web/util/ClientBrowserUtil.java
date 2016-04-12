/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.web.util;

import org.silverpeas.core.util.EncodeHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class ClientBrowserUtil {

  public static final Pattern MOZILLA = Pattern.compile("(?i).*gecko.*");
  public static final Pattern MOZILLA_LIKE = Pattern.compile("(?i).*like.gecko.*");
  public static final Pattern CHROME = Pattern.compile(".*[C,c][H,h][R,r][O,o][M,m][E,e].*");
  public static final Pattern MSIE = Pattern.compile(".*[M,m][S,s][I,i][E,e].*");
  public static final Pattern MICROSOFT = Pattern.compile(
      ".*[M,m][I,i][C,c][R,r][O,o][S,s][O,o][F,f][T,t].*");
  public static final Pattern OPERA = Pattern.compile(".*[O,o][P,p][E,e][R,r][A,a].*");
  public static final Pattern SAFARI = Pattern.compile(".*[S,s][A,a][F,f][A,a][R,r][I,i].*");
  public static final Pattern KONQUEROR = Pattern.compile(
      ".*[K,k][O,o][N,n][Q,q][U,u][E,e][R,r][O,o][R,r].*");
  public static final Pattern LINUX = Pattern.compile(".*[L,l][I,i][N,n][U,u][X,x].*");
  public static final Pattern WINDOWS = Pattern.compile(".*[W,w][I,ia][N,n].*");
  public static final Pattern MAC = Pattern.compile(".*[M,m][A,a][C,c].*");
  public static final Pattern FREEBSD = Pattern.compile(".*[F,f][R,r][E,e][E,e][B,b][S,s][D,d].*");
  public static final Pattern UNIX = Pattern.compile(".*[X,x][11].*");

  /**
   * Returns true if the user-agent indicates Windows.
   * @param request
   * @return true if the user-agent indicates Windows.
   */
  public static boolean isWindows(HttpServletRequest request) {
    return isWindows(getUserAgent(request));
  }

  /**
   * Returns true if the user-agent indicates Windows.
   * @param userAgent the request User-Agent header.
   * @return true if the user-agent indicates Windows.
   */
  public static boolean isWindows(String userAgent) {
    Matcher match = WINDOWS.matcher(userAgent);
    return match.matches();
  }

  /**
   * Returns true if the user-agent indicates MacOSX.
   * @param request
   * @return true if the user-agent indicates MacOSX.
   */
  public static boolean isMacintosh(HttpServletRequest request) {
    return isMacintosh(getUserAgent(request));
  }

  /**
   * Returns true if the user-agent indicates MacOSX.
   * @param userAgent the request User-Agent header.
   * @return true if the user-agent indicates MacOSX.
   */
  public static boolean isMacintosh(String userAgent) {
    Matcher match = MAC.matcher(userAgent);
    return match.matches();
  }

  /**
   * Returns true if the user-agent indicates Unix.
   * @param request
   * @return true if the user-agent indicates Unix.
   */
  public static boolean isUnix(HttpServletRequest request) {
    return isUnix(getUserAgent(request));
  }

  /**
   * Returns true if the user-agent indicates Unix.
   * @param userAgent the request User-Agent header.
   * @return true if the user-agent indicates Unix.
   */
  public static boolean isUnix(String userAgent) {
    Matcher matchBsd = FREEBSD.matcher(userAgent);
    Matcher matchLinux = LINUX.matcher(userAgent);
    Matcher matchUnix = UNIX.matcher(userAgent);
    return matchLinux.matches() || matchBsd.matches() || matchUnix.matches();
  }

  /**
   * Returns true if the user-agent indicates a Firefox browser.
   * @param request
   * @return true if the user-agent indicates a Firefox browser..
   */
  public static boolean isFirefox(HttpServletRequest request) {
    return isFirefox(getUserAgent(request));
  }

  /**
   * Returns true if the user-agent indicates a Firefox browser.
   * @param userAgent the request User-Agent header.
   * @return true if the user-agent indicates a Firefox browser..
   */
  public static boolean isFirefox(String userAgent) {
    Matcher matchMoz = MOZILLA.matcher(userAgent);
    Matcher matchMozLike = MOZILLA_LIKE.matcher(userAgent);
    Matcher matchChrome = CHROME.matcher(userAgent);
    return matchMoz.matches() && !matchMozLike.matches() && !matchChrome.matches();
  }

  /**
   * Returns true if the user-agent indicates a Safari browser.
   * @param request
   * @return true if the user-agent indicates a Safari browser..
   */
  public static boolean isSafari(HttpServletRequest request) {
    return isSafari(getUserAgent(request));
  }

  /**
   * Returns true if the user-agent indicates a Safari browser.
   * @param userAgent the request User-Agent header.
   * @return true if the user-agent indicates a Safari browser..
   */
  public static boolean isSafari(String userAgent) {
    Matcher matchSafari = SAFARI.matcher(userAgent);
    Matcher matchChrome = CHROME.matcher(userAgent);
    return matchSafari.matches() && !matchChrome.matches();
  }

  /**
   * @param request
   * @return
   */
  public static boolean isChrome(HttpServletRequest request) {
    return isChrome(getUserAgent(request));
  }

  /**
   * @param userAgent
   * @return
   */
  public static boolean isChrome(String userAgent) {
    Matcher matchChrome = CHROME.matcher(userAgent);
    return matchChrome.matches();
  }

  /**
   * @param request
   * @return
   */
  public static boolean isOpera(HttpServletRequest request) {
    return isOpera(getUserAgent(request));
  }

  /**
   * @param userAgent
   * @return
   */
  public static boolean isOpera(String userAgent) {
    Matcher match = OPERA.matcher(userAgent);
    return match.matches();
  }

  /**
   * @param request
   * @return
   */
  public static boolean isInternetExplorer(HttpServletRequest request) {
    return isInternetExplorer(getUserAgent(request));
  }

  /**
   * @param userAgent
   * @return
   */
  public static boolean isInternetExplorer(String userAgent) {
    Matcher matchIe = MSIE.matcher(userAgent);
    Matcher matchMs = MICROSOFT.matcher(userAgent);
    return matchIe.matches() || matchMs.matches();
  }

  /**
   * @param request
   * @return
   */
  public static boolean isKonqueror(HttpServletRequest request) {
    return isKonqueror(getUserAgent(request));
  }

  /**
   * @param userAgent
   * @return
   */
  public static boolean isKonqueror(String userAgent) {
    Matcher match = KONQUEROR.matcher(userAgent);
    return match.matches();
  }

  /**
   * @param request
   * @return
   */
  public static String getUserAgent(HttpServletRequest request) {
    return request.getHeader("User-Agent");
  }

  /**
   * @param request
   * @param filename
   * @return
   */
  public static String rfc2047EncodeFilename(HttpServletRequest request, String filename) {
    if (isFirefox(request) || isChrome(request) || isSafari(request)) {
      return EncodeHelper.encodeFilename(filename);
    }
    return filename;
  }

  /**
   * @param userAgent
   * @param filename
   * @return
   */
  public static String rfc2047EncodeFilename(String userAgent, String filename) {
    if (isFirefox(userAgent) || isChrome(userAgent) || isSafari(userAgent)) {
      return EncodeHelper.encodeFilename(filename);
    }
    return filename;
  }

  private ClientBrowserUtil() {
  }
}
