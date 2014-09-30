package org.silverpeas.util.security;

import org.silverpeas.util.ServiceProvider;

public class ContentEncryptionServiceProvider {

  protected ContentEncryptionServiceProvider() {
  }

  public static ContentEncryptionService getContentEncryptionService() {
    return ServiceProvider.getService(ContentEncryptionService.class);
  }
}
