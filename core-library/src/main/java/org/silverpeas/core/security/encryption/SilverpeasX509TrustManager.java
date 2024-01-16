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
package org.silverpeas.core.security.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

/**
 * This is our own implementation of X509TrustManager using the default one but specifying our own
 * truststore file thus enabling Silverpeas to configure this system element.
 * @author ehugonnet
 */
public class SilverpeasX509TrustManager implements X509TrustManager {

  final Logger logger = LoggerFactory.getLogger(SilverpeasX509TrustManager.class);
  private X509TrustManager defaultTrustManager;

  public SilverpeasX509TrustManager(String trustStoreFile, char[] password) {
    try {
      KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
      initTrustManager(trustStoreFile, truststore, password);
    } catch (GeneralSecurityException secEx) {
      logger.error(String.format("Couldn't create truststore %s", trustStoreFile), secEx);
    }
  }

  private void initTrustManager(final String trustStoreFile, final KeyStore keyStore,
      final char[] password) throws GeneralSecurityException {
    try (InputStream fis = new FileInputStream(trustStoreFile)) {
      keyStore.load(fis, password);
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
      tmf.init(keyStore);
      TrustManager[] tms = tmf.getTrustManagers();
      defaultTrustManager = Stream.of(tms)
          .filter(t -> t instanceof X509TrustManager)
          .map(t -> (X509TrustManager) t)
          .findFirst()
          .orElseThrow(() -> new GeneralSecurityException("No X509 TrustStore Manager found!"));
    } catch (IOException ioex) {
      logger.error(String.format("Couldn't load truststore %s", trustStoreFile), ioex);
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
