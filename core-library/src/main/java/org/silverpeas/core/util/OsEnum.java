/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

/**
 * @author Emmanuel Hugonnet
 */
public enum OsEnum {

  WINDOWS_XP("Windows XP", true), WINDOWS_9X("win9x", true), WINDOWS_VISTA("Windows Vista", true),
  WINDOWS_SEVEN("Windows 7", true), LINUX("Linux", false), MAC_OSX("Mac OS X", false),
  OS_400("os/400", false), Z_OS("z/os", false), OPENVMS("openvms", false), NETWARE("netware", false);
  protected final boolean windows;
  protected final String name;

  OsEnum(String name, boolean windows) {
    this.windows = windows;
    this.name = name;
  }

  public static OsEnum getOS(String value) {
    if (WINDOWS_VISTA.name.equalsIgnoreCase(value)) {
      return WINDOWS_VISTA;
    }
    if (WINDOWS_SEVEN.name.equalsIgnoreCase(value)) {
      return WINDOWS_SEVEN;
    }
    if (WINDOWS_9X.name.equalsIgnoreCase(value)) {
      return WINDOWS_9X;
    }
    if (WINDOWS_XP.name.equalsIgnoreCase(value) || value.startsWith("Windows ")) {
      return WINDOWS_XP;
    }
    if (LINUX.name.equalsIgnoreCase(value)) {
      return LINUX;
    }
    if (MAC_OSX.name.equalsIgnoreCase(value)) {
      return MAC_OSX;
    }
    if (OS_400.name.equalsIgnoreCase(value)) {
      return OS_400;
    }
    if (Z_OS.name.equalsIgnoreCase(value)) {
      return Z_OS;
    }
    if (NETWARE.name.equalsIgnoreCase(value)) {
      return NETWARE;
    }
    return WINDOWS_XP;
  }

  public static OsEnum getOS() {
    return getOS(System.getProperty("os.name"));
  }

  public boolean isWindows() {
    return this.windows;
  }
}
