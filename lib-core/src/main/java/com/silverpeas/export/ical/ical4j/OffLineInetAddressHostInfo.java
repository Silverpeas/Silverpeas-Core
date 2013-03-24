package com.silverpeas.export.ical.ical4j;

import com.stratelia.webactiv.util.GeneralPropertiesManager;
import net.fortuna.ical4j.util.HostInfo;
import net.fortuna.ical4j.util.InetAddressHostInfo;

import java.net.SocketException;

public class OffLineInetAddressHostInfo implements HostInfo {
  private InetAddressHostInfo hostInfo;

  @Override
  public String getHostName() {
    String hostName;
    try {
      hostName = getInetAddressHostInfo().getHostName();
    } catch (SocketException ex) {
      hostName = GeneralPropertiesManager.getString("httpServerBase" , "localhost");
    } catch (NullPointerException ex) {
      hostName = GeneralPropertiesManager.getString("httpServerBase" , "localhost");
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
