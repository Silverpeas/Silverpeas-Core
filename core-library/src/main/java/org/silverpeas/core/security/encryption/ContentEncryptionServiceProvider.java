package org.silverpeas.core.security.encryption;

import org.silverpeas.core.util.ServiceProvider;

public class ContentEncryptionServiceProvider {

  protected ContentEncryptionServiceProvider() {
  }

  public static ContentEncryptionService getContentEncryptionService() {
    return ServiceProvider.getService(ContentEncryptionService.class);
  }
}
