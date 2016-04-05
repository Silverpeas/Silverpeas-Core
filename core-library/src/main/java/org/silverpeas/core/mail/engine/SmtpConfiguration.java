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

package org.silverpeas.core.mail.engine;

import org.silverpeas.core.util.MailUtil;

/**
 * SMTP configuration
 * @author Emmanuel Hugonnet
 */
public class SmtpConfiguration {
  public static final String SECURE_TRANSPORT = "smtps";
  public static final String SIMPLE_TRANSPORT = "smtp";

  private String username;
  private String password;
  private String server;
  private boolean secure;
  private int port;
  private boolean authenticate;
  private boolean debug;

  /**
   * Gets an instance of {@link SmtpConfiguration} initializes with the data provided by {@link
   * MailUtil}.
   * @return a new instance of {@link SmtpConfiguration}
   */
  public static SmtpConfiguration fromDefaultSettings() {
    SmtpConfiguration smtpConfiguration = new SmtpConfiguration();
    smtpConfiguration.setUsername(MailUtil.getLogin());
    smtpConfiguration.setPassword(MailUtil.getPassword());
    smtpConfiguration.setServer(MailUtil.getMailServer());
    smtpConfiguration.setSecure(MailUtil.isSecure());
    smtpConfiguration.setPort(MailUtil.getPort());
    smtpConfiguration.setAuthenticate(MailUtil.isAuthenticated());
    smtpConfiguration.setDebug(MailUtil.isDebug());
    return smtpConfiguration;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isAuthenticate() {
    return authenticate;
  }

  public void setAuthenticate(boolean authenticate) {
    this.authenticate = authenticate;
  }

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(final boolean debug) {
    this.debug = debug;
  }
}
