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
    } catch (SocketException ex) {
      hostName = ResourceLocator.getGeneralSettingBundle().getString("httpServerBase", "localhost");
    } catch (NullPointerException ex) {
      hostName = ResourceLocator.getGeneralSettingBundle().getString("httpServerBase", "localhost");
    }
    return hostName;
  }

   private synchronized InetAddressHostInfo getInetAddressHostInfo() throws SocketException {
      if(hostInfo == null) {
        hostInfo = new InetAddressHostInfo();
      }
     return hostInfo;
   }
}
