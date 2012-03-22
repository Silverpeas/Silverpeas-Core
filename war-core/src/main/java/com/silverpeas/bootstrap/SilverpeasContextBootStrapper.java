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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.bootstrap;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.security.SilverpeasSSLSocketFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SilverpeasContextBootStrapper implements ServletContextListener {

  private ContextLoaderListener springContextListener = new ContextLoaderListener();

  @Override
  /**
   * Do nothing
   */
  public void contextDestroyed(ServletContextEvent sce) {
    springContextListener.contextDestroyed(sce);
  }

  /**
   * Initialise the System.properties according to Silverpeas needs and configuration.
   * @param sce
   */
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ResourceBundle silverpeasInitialisationSettings = FileUtil.loadBundle(
        "com.stratelia.silverpeas._silverpeasinitialize.settings._silverpeasinitializeSettings",
        new Locale("fr", ""));
    Security.addProvider(new BouncyCastleProvider());
    File pathInitialize = new File(silverpeasInitialisationSettings.getString("pathInitialize"));
    if (pathInitialize == null) {
      Logger.getLogger("bootstrap").log(Level.SEVERE,
          "Repository Initialize for systemSettings.properties file is not defined in Settings.");
    } else {
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(new File(pathInitialize, "systemSettings.properties"));
        Properties systemFileProperties = new Properties(System.getProperties());
        systemFileProperties.load(fis);

        // Fix - empty proxy port and proxy host not supported by Spring Social
        if (!StringUtil.isDefined(systemFileProperties.getProperty("http.proxyPort"))) {
          systemFileProperties.remove("http.proxyPort");
        }
        if (!StringUtil.isDefined(systemFileProperties.getProperty("http.proxyHost"))) {
          systemFileProperties.remove("http.proxyHost");
        }

        System.setProperties(systemFileProperties);
        if (isTrustoreConfigured()) {
          registerSSLSocketFactory();
        }
      } catch (FileNotFoundException e) {
        Logger.getLogger("bootstrap").log(Level.SEVERE,
            "File systemSettings.properties in directory {0} not found.", pathInitialize);
      } catch (IOException e) {
        Logger.getLogger("bootstrap")
            .log(Level.SEVERE, "Unable to read systemSettings.properties.");
      } catch (GeneralSecurityException e) {
        Logger.getLogger("bootstrap").log(Level.SEVERE, "Unable to configure the trustore.");
      } finally {
        IOUtils.closeQuietly(fis);
      }

    }
    springContextListener.contextInitialized(sce);
  }

  boolean isTrustoreConfigured() {
    return StringUtil.isDefined(System.getProperty(SilverpeasSSLSocketFactory.TRUSTSTORE_KEY));
  }

  /**
   * Adding SilverpeasSSLSocketFactory as the SSLSocketFactory for all mail protocoles.
   * @throws GeneralSecurityException
   */
  void registerSSLSocketFactory() throws GeneralSecurityException {
    System.setProperty("mail.imap.ssl.enable", "true");
    System.setProperty("mail.imap.ssl.socketFactory.class",
        "com.silverpeas.util.security.SilverpeasSSLSocketFactory");
    System.setProperty("mail.smtp.ssl.socketFactory.class",
        "com.silverpeas.util.security.SilverpeasSSLSocketFactory");
    System.setProperty("mail.smtps.ssl.socketFactory.class",
        "com.silverpeas.util.security.SilverpeasSSLSocketFactory");
    System.setProperty("mail.pop3.ssl.socketFactory.class",
        "com.silverpeas.util.security.SilverpeasSSLSocketFactory");
    System.setProperty("mail.pop3s.ssl.socketFactory.class",
        "com.silverpeas.util.security.SilverpeasSSLSocketFactory");
    System.setProperty("mail.imaps.ssl.socketFactory.class",
        "com.silverpeas.util.security.SilverpeasSSLSocketFactory");
    System.setProperty("mail.imap.ssl.socketFactory.fallback", "false");
  }
}
