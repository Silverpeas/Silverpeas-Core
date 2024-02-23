package org.silverpeas.core.admin.domain.driver.ldapdriver;

import org.silverpeas.core.admin.domain.driver.DomainDescriptor;
import org.silverpeas.core.security.encryption.EncryptionContentIterator;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.StringUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An iterator on all the LDAP domains to encrypt or to renew encryption of the credentials used to
 * access the remote LDAP service.
 * @author mmoquillon
 */
public class LDAPCredentialsEncryptionIterator implements EncryptionContentIterator {

  private static final String AUT_SERVER = "autServer";
  private static final String LOGIN = "LOGIN";
  private static final String PASSWORD = "PASSWORD";
  private Iterator<DomainDescriptor> domainDescriptors = new Iterator<>() {
    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public DomainDescriptor next() {
      throw new NoSuchElementException();
    }
  };

  private final Path domainsPath;
  private DomainDescriptor currentDomain;

  public LDAPCredentialsEncryptionIterator() {
    Path root = Path.of(SystemWrapper.get().getenv("SILVERPEAS_HOME"), "properties");
    domainsPath = root.resolve(Path.of("org", "silverpeas", "domains"));
  }

  @Override
  public void init() {
    Function<Path, DomainDescriptor> loadProperties = p -> {
      String domainName = p.toFile().getName().replaceFirst("domain", "").replace(".properties", "");
      DomainDescriptor descriptor = new DomainDescriptor(domainName);
      descriptor.loadDomainProperties();
      return descriptor;
    };

    Predicate<DomainDescriptor> hasEncryptedCredentials = p ->
      StringUtil.getBooleanValue(
          p.getDomainProperties().getProperty("database.encryptedCredentials", "false"));

    Predicate<Path> isADomainDescriptor = p -> p.toFile().getName().startsWith("domain");

    try(Stream<Path> streamChildren = Files.list(domainsPath)) {
      var domainsWithEncryptedCredentials = streamChildren
          .filter(Files::isRegularFile)
          .filter(isADomainDescriptor)
          .map(loadProperties)
          .filter(hasEncryptedCredentials)
          .collect(Collectors.toSet());
      domainDescriptors = domainsWithEncryptedCredentials.iterator();
    } catch (IOException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Override
  public Map<String, String> next() {
    currentDomain = domainDescriptors.next();
    Properties properties = currentDomain.getDomainProperties();
    Map<String, String> credentials = new HashMap<>();
    credentials.put(LOGIN, properties.getProperty("database.LDAPAccessLoginDN"));
    credentials.put(PASSWORD, properties.getProperty("database.LDAPAccessPasswd"));
    return credentials;
  }

  @Override
  public boolean hasNext() {
    return domainDescriptors.hasNext();
  }

  @Override
  public void update(Map<String, String> updatedContent) {
    String login = updatedContent.get(LOGIN);
    String password = updatedContent.get(PASSWORD);

    Properties properties = new Properties();
    properties.setProperty("database.LDAPAccessLoginDN", login);
    properties.setProperty("database.LDAPAccessPasswd", password);
    currentDomain.updateDomainProperties(properties);

    currentDomain.loadAuthenticationProperties();
    Properties authProperties = currentDomain.getAuthenticationProperties();
    properties.clear();
    int serverCount = Integer.parseInt(authProperties.getProperty("autServersCount"));
    for (int i = 0; i < serverCount; i++) {
      boolean isCredentialsEncrypted = StringUtil.getBooleanValue(
          authProperties.getProperty(AUT_SERVER + i + ".encryptedCredentials", "false"));
      if (isCredentialsEncrypted) {
        properties.setProperty(AUT_SERVER + i + ".LDAPAccessLogin", login);
        properties.setProperty(AUT_SERVER + i + ".LDAPAccessPasswd", password);
      }
    }
    currentDomain.updateAuthenticationProperties(properties);
  }

  @Override
  public void onError(Map<String, String> content, CryptoException ex) {
    SilverLogger.getLogger(this).error(ex.getMessage());
  }
}
  