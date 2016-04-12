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
package org.silverpeas.web.bootstrap;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.security.encryption.SilverpeasSSLSocketFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.security.GeneralSecurityException;

public class SilverpeasContextBootStrapper implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
  }

  /**
   * Loads the system settings from the systemSettings.properties and apply the corresponding
   * configuration. It is mainly used to perform the proxy and SSL setting up.
   *
   * @param sce
   */
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    SilverLogger.getLogger("silverpeas").info("Silverpeas System Initialization...");
    try {
      if (isTrustoreConfigured()) {
          registerSSLSocketFactory();
        }
    } catch (GeneralSecurityException e) {
      SilverLogger.getLogger("silverpeas").error("Unable to configure the keystore/truststore.");
    }

    URLUtil.setSilverpeasVersion(sce.getServletContext().getInitParameter("SILVERPEAS_VERSION"));
  }

  boolean isTrustoreConfigured() {
    return StringUtil.isDefined(
        SystemWrapper.get().getProperty(SilverpeasSSLSocketFactory.TRUSTSTORE_KEY));
  }

  /**
   * Adding SilverpeasSSLSocketFactory as the SSLSocketFactory for all mail protocoles.
   *
   * @throws GeneralSecurityException
   */
  void registerSSLSocketFactory() throws GeneralSecurityException {
    SystemWrapper.get().setProperty("mail.imap.ssl.enable", "true");
    SystemWrapper.get().setProperty("mail.imap.ssl.socketFactory.class",
        "org.silverpeas.core.security.encryption.SilverpeasSSLSocketFactory");
    SystemWrapper.get().setProperty("mail.smtp.ssl.socketFactory.class",
        "org.silverpeas.core.security.encryption.SilverpeasSSLSocketFactory");
    SystemWrapper.get().setProperty("mail.smtps.ssl.socketFactory.class",
        "org.silverpeas.core.security.encryption.SilverpeasSSLSocketFactory");
    SystemWrapper.get().setProperty("mail.pop3.ssl.socketFactory.class",
        "org.silverpeas.core.security.encryption.SilverpeasSSLSocketFactory");
    SystemWrapper.get().setProperty("mail.pop3s.ssl.socketFactory.class",
        "org.silverpeas.core.security.encryption.SilverpeasSSLSocketFactory");
    SystemWrapper.get().setProperty("mail.imaps.ssl.socketFactory.class",
        "org.silverpeas.core.security.encryption.SilverpeasSSLSocketFactory");
    SystemWrapper.get().setProperty("mail.imap.ssl.socketFactory.fallback", "false");
  }
}
