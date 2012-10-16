/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.util;

/**
 * @author Emmanuel Hugonnet
 */
public enum OsEnum {

  WINDOWS_XP(true), WINDOWS_VISTA(true), WINDOWS_SEVEN(true), LINUX(false), MAC_OSX(false);
  protected boolean windows;

  OsEnum(boolean windows) {
    this.windows = windows;
  }

  public static OsEnum getOS(String value) {
    if ("Windows Vista".equalsIgnoreCase(value)) {
      return WINDOWS_VISTA;
    }
    if ("Windows 7".equalsIgnoreCase(value)) {
      return WINDOWS_SEVEN;
    }
    if ("Windows XP".equalsIgnoreCase(value) || value.startsWith("Windows ")) {
      return WINDOWS_XP;
    }
    if ("Linux".equalsIgnoreCase(value)) {
      return LINUX;
    }
    return MAC_OSX;
  }

  public static OsEnum getOS() {
    return getOS(System.getProperty("os.name"));
  }

  public boolean isWindows() {
    return this.windows;
  }
}
