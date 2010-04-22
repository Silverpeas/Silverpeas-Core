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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class ClientBrowserUtil {

  public static final Pattern MOZILLA = Pattern
      .compile(".*[G,g][E,e][C,c][K,k][O,o].*");
  public static final Pattern MSIE = Pattern
      .compile(".*[M,m][S,s][I,i][E,e].*");
  public static final Pattern MICROSOFT = Pattern
      .compile(".*[M,m][I,i][C,c][R,r][O,o][S,s][O,o][F,f][T,t].*");
  public static final Pattern OPERA = Pattern
      .compile(".*[O,o][P,p][E,e][R,r][A,a].*");
  public static final Pattern SAFARI = Pattern
      .compile(".*[S,s][A,a][F,f][A,a][R,r][I,i].*");
  public static final Pattern KONQUEROR = Pattern
      .compile(".*[K,k][O,o][N,n][Q,q][U,u][E,e][R,r][O,o][R,r].*");

  public static final Pattern LINUX = Pattern
      .compile(".*[L,l][I,i][N,n][U,u][X,x].*");
  public static final Pattern WINDOWS = Pattern.compile(".*[W,w][I,ia][N,n].*");
  public static final Pattern MAC = Pattern.compile(".*[M,m][A,a][C,c].*");
  public static final Pattern FREEBSD = Pattern
      .compile(".*[F,f][R,r][E,e][E,e][B,b][S,s][D,d].*");
  public static final Pattern UNIX = Pattern.compile(".*[X,x][11].*");

  public static final boolean isWindows(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    Matcher match = WINDOWS.matcher(userAgent);
    return match.matches();
  }

  public static final boolean isMacintosh(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    Matcher match = MAC.matcher(userAgent);
    return match.matches();
  }

  public static final boolean isUnix(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    Matcher matchBsd = FREEBSD.matcher(userAgent);
    Matcher matchLinux = LINUX.matcher(userAgent);
    Matcher matchUnix = UNIX.matcher(userAgent);
    return matchLinux.matches() || matchBsd.matches() || matchUnix.matches();
  }

  public static final boolean isFirefox(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    Matcher match = MOZILLA.matcher(userAgent);
    return match.matches();
  }

  public static final boolean isSafari(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    Matcher match = SAFARI.matcher(userAgent);
    return match.matches();
  }

  public static final boolean isOpera(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    Matcher match = OPERA.matcher(userAgent);
    return match.matches();
  }

  public static final boolean isInternetExplorer(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    Matcher matchIe = MSIE.matcher(userAgent);
    Matcher matchMs = MICROSOFT.matcher(userAgent);
    return matchIe.matches() || matchMs.matches();
  }

  public static final boolean isKonqueror(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    Matcher match = KONQUEROR.matcher(userAgent);
    return match.matches();
  }
}
