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
package org.silverpeas.core.importexport.ical.ical4j;

import net.fortuna.ical4j.util.HostInfo;
import net.fortuna.ical4j.util.InetAddressHostInfo;
import org.silverpeas.core.util.ResourceLocator;

import java.net.SocketException;

public class OffLineInetAddressHostInfo implements HostInfo {
  private InetAddressHostInfo hostInfo;

  @Override
  public String getHostName() {
    String hostName;
    try {
      hostName = getInetAddressHostInfo().getHostName();
    } catch (SocketException | NullPointerException ex) {
      hostName = ResourceLocator.getGeneralSettingBundle().getString("httpServerBase", "localhost");
    }
    return hostName;
  }

  private synchronized InetAddressHostInfo getInetAddressHostInfo() throws SocketException {
    if (hostInfo == null) {
      hostInfo = new InetAddressHostInfo();
    }
    return hostInfo;
  }
}
