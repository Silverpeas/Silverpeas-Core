package com.silverpeas.util.security;

import javax.inject.Inject;

public class ContentEncryptionServiceFactory {

  private static ContentEncryptionServiceFactory instance = new ContentEncryptionServiceFactory();
  
  protected ContentEncryptionServiceFactory() {
    
  }
  
  @Inject
  private ContentEncryptionService service;
  
  public static ContentEncryptionServiceFactory getFactory() {
    return instance;
  }
  
  public ContentEncryptionService getContentEncryptionService() {
    return service;
  }
}
