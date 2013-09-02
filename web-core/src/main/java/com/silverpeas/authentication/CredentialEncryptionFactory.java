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

package com.silverpeas.authentication;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Factory of CredentialEncryption instances. It constructs an instance from the correct
 * encryption implementation after identifying it from the settings.
 */
public class CredentialEncryptionFactory {

  private Class<? extends CredentialEncryption> encryptionClass;
  private static final CredentialEncryptionFactory instance = new CredentialEncryptionFactory();

  /**
   * -------------------------------------------------------------------------- constructor
   */
  private CredentialEncryptionFactory() {
    ResourceLocator settingsFile = new ResourceLocator(
        "com.silverpeas.authentication.settings.authenticationSettings", "");
    try {
      encryptionClass =
          (Class<? extends CredentialEncryption>) Class.forName(settingsFile.getString(
          "encryptionClass", "com.silverpeas.authentication.CustomCredentialEncryption"));

    } catch (ClassNotFoundException e) {
      SilverTrace.info("authentication", "CredentialEncryptionFactory.getCustomEncryption()",
          "root.MSG_PARAM_ENTER_VALUE", "Encrypt/Decrypt Custom Class not found", e);
    }
  }

  public static CredentialEncryptionFactory getInstance() {
    return instance;
  }

  /**
   * Get standard Encryption class
   * @return
   */
  public CredentialEncryption getEncryption() {
    try {
      return encryptionClass.newInstance();
    } catch (InstantiationException e) {
      SilverTrace.info("authentication", "CredentialEncryptionFactory.getCustomEncryption()",
          "root.MSG_PARAM_ENTER_VALUE", "Encrypt/Decrypt Custom Class not found", e);
    } catch (IllegalAccessException e) {
      SilverTrace.info("authentication", "CredentialEncryptionFactory.getCustomEncryption()",
          "root.MSG_PARAM_ENTER_VALUE", "Encrypt/Decrypt Custom Class not found", e);
    }
    return new CustomCredentialEncryption();
  }
}