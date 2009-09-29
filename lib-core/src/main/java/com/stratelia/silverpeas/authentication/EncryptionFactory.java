package com.stratelia.silverpeas.authentication;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

public class EncryptionFactory {
  /**
   * --------------------------------------------------------------------------
   * constructor
   */
  public EncryptionFactory() {
  }

  /**
   * Get standard Encryption class
   * 
   * @return
   */
  public EncryptionInterface getEncryption() {
    EncryptionInterface encryptionInterface = getCustomEncryption();
    if (encryptionInterface == null)
      encryptionInterface = new AuthenticationEncrypt();
    return encryptionInterface;
  }

  /**
   * Get custom Encryption class
   * 
   * @return
   */
  private EncryptionInterface getCustomEncryption() {
    ResourceLocator settingsFile = new ResourceLocator(
        "com.silverpeas.authentication.settings.authenticationSettings.properties",
        "");
    EncryptionInterface encryptionInterface = null;
    try {
      Class encryptionClass = Class.forName(settingsFile
          .getString("encryptionClass"));
      encryptionInterface = (EncryptionInterface) encryptionClass.newInstance();
    } catch (Exception e) {
      SilverTrace.info("authentication",
          "EncryptionFactory.getCustomEncryption()",
          "root.MSG_PARAM_ENTER_VALUE",
          "Encrypt/Decrypt Custom Class not found");
    }
    return encryptionInterface;
  }
}