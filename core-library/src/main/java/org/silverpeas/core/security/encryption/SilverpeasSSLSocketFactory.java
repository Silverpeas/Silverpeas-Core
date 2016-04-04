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

package org.silverpeas.core.security.encryption;

import org.silverpeas.core.util.StringUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * This is our own implementation of SSLSocketFactory using the default one but specifying our own
 * truststore file thus enabling Silverpeas to configure this system element. The TrustStore is the
 * one declared in Silverpeas, not the one in the default System.properties at launch time.
 * @author ehugonnet
 */
public class SilverpeasSSLSocketFactory extends SSLSocketFactory {

  public static final String TRUSTSTORE_KEY = "javax.net.ssl.trustStore";
  public static final String TRUSTSTORE_PASSWORD_KEY = "javax.net.ssl.trustStorePassword";
  private SSLSocketFactory factory;

  private char[] getTrustorePassword() {
    char[] password = new char[0];
    if (StringUtil.isDefined(System.getProperty(TRUSTSTORE_PASSWORD_KEY))) {
      password = System.getProperty(TRUSTSTORE_PASSWORD_KEY).toCharArray();
    }
    return password;
  }

  public SilverpeasSSLSocketFactory() {
    try {
      SSLContext sslcontext = SSLContext.getInstance("TLS");
      sslcontext.init(null,
          new TrustManager[] { new SilverpeasX509TrustManager(System.getProperty(TRUSTSTORE_KEY),
          getTrustorePassword()) }, null);
      factory = sslcontext.getSocketFactory();
    } catch (Exception ex) {
      // ignore
    }
  }

  public static SocketFactory getDefault() {
    return new SilverpeasSSLSocketFactory();
  }

  @Override
  public Socket createSocket() throws IOException {
    return factory.createSocket();
  }

  @Override
  public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException {
    return factory.createSocket(socket, s, i, flag);
  }

  @Override
  public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1, int j) throws
      IOException {
    return factory.createSocket(inaddr, i, inaddr1, j);
  }

  @Override
  public Socket createSocket(InetAddress inaddr, int i) throws IOException {
    return factory.createSocket(inaddr, i);
  }

  @Override
  public Socket createSocket(String s, int i, InetAddress inaddr, int j) throws IOException {
    return factory.createSocket(s, i, inaddr, j);
  }

  @Override
  public Socket createSocket(String s, int i) throws IOException {
    return factory.createSocket(s, i);
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return factory.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return factory.getSupportedCipherSuites();
  }
}
