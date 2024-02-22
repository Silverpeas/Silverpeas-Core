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
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 *
 * @author ehugonnet
 */
public class LdapConfiguration {

  private boolean encryptedCredentials = false;

  private String ldapHost = "localhost";
  private int ldapPort = 389;
  private String username;
  private String password;
  private int timeout = 0;

  public void setEncryptedCredentials(boolean encryptedCredentials) {
    this.encryptedCredentials = encryptedCredentials;
  }

  /**
   * Get the value of timeout
   *
   * @return the value of timeout
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Set the value of timeout
   *
   * @param timeout new value of timeout
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
  private boolean secure = false;

  /**
   * Get the value of secure
   *
   * @return the value of secure
   */
  public boolean isSecure() {
    return secure;
  }

  /**
   * Set the value of secure
   *
   * @param secure new value of secure
   */
  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  /**
   * Get the value of password
   *
   * @return the value of password
   */
  public byte[] getPassword() {
    return decryptIfNeeded(password).getBytes(Charsets.UTF_8);
  }

  /**
   * Set the value of password
   *
   * @param password new value of password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Get the value of ldapHost
   *
   * @return the value of ldapHost
   */
  public String getLdapHost() {
    return ldapHost;
  }

  /**
   * Set the value of ldapHost
   *
   * @param ldapHost new value of ldapHost
   */
  public void setLdapHost(String ldapHost) {
    this.ldapHost = ldapHost;
  }

  /**
   * Get the value of ldapPort
   *
   * @return the value of ldapPort
   */
  public int getLdapPort() {
    return ldapPort;
  }

  /**
   * Set the value of ldapPort
   *
   * @param ldapPort new value of ldapPort
   */
  public void setLdapPort(int ldapPort) {
    this.ldapPort = ldapPort;
  }

  /**
   * Get the value of username
   *
   * @return the value of username
   */
  public String getUsername() {
    return decryptIfNeeded(this.username);
  }

  /**
   * Set the value of username
   *
   * @param username new value of username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String toString() {
    //noinspection
    return "LdapConfiguration{" + "ldapHost=" + ldapHost + ", ldapPort=" + ldapPort + ", username="
        + username + ", password=" + password + ", timeout=" + timeout + ", secure=" + secure + '}';
  }

  private String decryptIfNeeded(String possibleEncryptedText) {
    try {
      return encryptedCredentials ?
          ContentEncryptionService.get().decryptContent(possibleEncryptedText)[0] :
          possibleEncryptedText;
    } catch (CryptoException | IndexOutOfBoundsException e) {
      String additionalInfo = e.getCause() != null ? "Cause: " + e.getCause().getMessage() : "";
      SilverLogger.getLogger(this).error(e.getMessage() + " " + additionalInfo);
      return possibleEncryptedText;
    }
  }

}
