/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.authentication;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

public class EncryptionFactory {

  private static final EncryptionFactory instance = new EncryptionFactory();
  /**
   * -------------------------------------------------------------------------- constructor
   */
  private EncryptionFactory() {
  }

  public static EncryptionFactory getInstance() {
    return instance;
  }

  /**
   * Get standard Encryption class
   * @return
   */
  public EncryptionInterface getEncryption() {
   ResourceLocator settingsFile = new ResourceLocator(
        "com.silverpeas.authentication.settings.authenticationSettings.properties", "");
    try {
      Class<? extends EncryptionInterface> encryptionClass = (Class<? extends EncryptionInterface>)
          Class.forName(settingsFile.getString("encryptionClass"));
      return encryptionClass.newInstance();
    } catch (ClassNotFoundException e) {
       SilverTrace.info("authentication", "EncryptionFactory.getCustomEncryption()",
          "root.MSG_PARAM_ENTER_VALUE", "Encrypt/Decrypt Custom Class not found", e);
    } catch (InstantiationException e) {
      SilverTrace.info("authentication", "EncryptionFactory.getCustomEncryption()",
          "root.MSG_PARAM_ENTER_VALUE", "Encrypt/Decrypt Custom Class not found", e);
    } catch (IllegalAccessException e) {
      SilverTrace.info("authentication", "EncryptionFactory.getCustomEncryption()",
          "root.MSG_PARAM_ENTER_VALUE", "Encrypt/Decrypt Custom Class not found", e);
    }
    return new AuthenticationEncrypt();
  }
}