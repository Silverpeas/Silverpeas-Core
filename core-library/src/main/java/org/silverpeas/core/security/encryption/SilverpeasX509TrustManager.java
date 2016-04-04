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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is our own implementation of X509TrustManager using the default one but specifying our own
 * truststore file thus enabling Silverpeas to configure this system element.
 * @author ehugonnet
 */
public class SilverpeasX509TrustManager implements X509TrustManager {

  final Logger logger = LoggerFactory.getLogger(SilverpeasX509TrustManager.class);
  private X509TrustManager defaultTrustManager;

  public SilverpeasX509TrustManager(String trustStoreFile, char[] password) {
    InputStream fis = null;
    try {
      KeyStore trustore = KeyStore.getInstance(KeyStore.getDefaultType());
      fis = new FileInputStream(trustStoreFile);
      trustore.load(fis, password);
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
      tmf.init(trustore);
      TrustManager tms[] = tmf.getTrustManagers();
      for (TrustManager trustManager : tms) {
        if (trustManager instanceof X509TrustManager) {
          defaultTrustManager = (X509TrustManager) trustManager;
          return;
        }
      }
    } catch (IOException ioex) {
      logger.error("Couldn't load trustore " + trustStoreFile, ioex);
    } catch (GeneralSecurityException secEx) {
      logger.error("Couldn't create trustore " + trustStoreFile, secEx);
    } finally {
      IOUtils.closeQuietly(fis);
    }

  }

  @Override
  public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
    defaultTrustManager.checkClientTrusted(xcs, string);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
    defaultTrustManager.checkServerTrusted(xcs, string);
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return defaultTrustManager.getAcceptedIssuers();
  }
}
