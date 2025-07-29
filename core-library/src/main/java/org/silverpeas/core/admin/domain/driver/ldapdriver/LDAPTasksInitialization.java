package org.silverpeas.core.admin.domain.driver.ldapdriver;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Singleton;

/**
 * Initializes all background tasks on the LDAP domains.
 * @author mmoquillon
 */
@Service
@Singleton
public class LDAPTasksInitialization implements Initialization {
  @Override
  public void init() {
    SilverLogger.getLogger(this).info("Register the dictionary access credentials encryption");
    LDAPCredentialsEncryptionIterator iterator = new LDAPCredentialsEncryptionIterator();
    ContentEncryptionService.get().registerForContentCiphering(iterator);
  }
}
  