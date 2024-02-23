package org.silverpeas.core.admin.domain.driver;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.file.FileUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Descriptor of a domain. It is made up of two properties file: one describing the domain itself,
 * how to access it and some additional properties, and the other one defining the authentication
 * servers to use when authenticating a user of the domain.
 *
 * @author mmoquillon
 */
public class DomainDescriptor {

  private final String domainSettingsPath;
  private final String authSettingsPath;

  private Properties domainSettings = new Properties();
  private Properties authSettings = new Properties();

  /**
   * Constructs a new descriptor of a domain with the specified name. The name is used to find the
   * different properties file of the domain.
   *
   * @param domainName the name of a domain.
   */
  public DomainDescriptor(final String domainName) {
    this.domainSettingsPath = "org.silverpeas.domains.domain" + domainName;
    this.authSettingsPath = "org.silverpeas.authentication.autDomain" + domainName;
  }

  /**
   * Loads only the properties describing the domain itself.
   *
   * @throws UncheckedIOException if an error occurs while loading the domain properties.
   */
  public void loadDomainProperties() throws UncheckedIOException {
    try {
      domainSettings = ResourceLocator.getSettingBundle(domainSettingsPath).asProperties();
    } catch (MissingResourceException e) {
      throw new UncheckedIOException(new IOException(e.getMessage()));
    }
  }

  /**
   * Loads only the authentication properties of the domain.
   *
   * @throws UncheckedIOException if an error occurs while loading the authentication properties.
   */
  public void loadAuthenticationProperties() throws UncheckedIOException {
    try {
      authSettings = ResourceLocator.getSettingBundle(authSettingsPath).asProperties();
    } catch (MissingResourceException e) {
      throw new UncheckedIOException(new IOException(e.getMessage()));
    }
  }

  /**
   * Gets the properties defining the domain itself and its parameters. It the properties were not
   * previously loaded, then the returned properties will be empty.
   *
   * @return the domain properties.
   */
  @Nonnull
  public Properties getDomainProperties() {
    return domainSettings;
  }

  /**
   * Gets the authentication properties of the domain. It the properties were not previously loaded,
   * then the returned properties will be empty.
   *
   * @return the authentication properties.
   */
  @Nonnull
  public Properties getAuthenticationProperties() {
    return authSettings;
  }

  /**
   * Updates the domain properties with the specified ones. New properties are ignored; only
   * existing properties are updated with their new value.
   *
   * @param domainProperties the new valued properties with which the existing ones will be
   * updated.
   * @throws UncheckedIOException if an error occurs while storing the new properties value into the
   * properties file.
   */
  public void updateDomainProperties(final Properties domainProperties)
      throws UncheckedIOException {
    if (domainProperties != null && !domainProperties.isEmpty()) {
      update(toFileSystemPath(domainSettingsPath), domainProperties);
    }
  }

  /**
   * Updates the authentication properties with the specified ones. New properties are ignored; only
   * existing properties are updated with their new value.
   *
   * @param authProperties the new valued properties with which the existing ones will be updated.
   * @throws UncheckedIOException if an error occurs while storing the new properties value into the
   * properties file.
   */
  public void updateAuthenticationProperties(final Properties authProperties)
      throws UncheckedIOException {
    if (authProperties != null && !authProperties.isEmpty()) {
      update(toFileSystemPath(authSettingsPath), authProperties);
    }
  }

  private void update(final Path propertiesFile, final Properties properties)
      throws UncheckedIOException {
    Path tmpPath = propertiesFile.getParent().resolve(propertiesFile.toFile().getName() + ".tmp");
    try (PrintWriter writer = new PrintWriter(tmpPath.toFile(), Charsets.ISO_8859_1)) {
      try (Stream<String> lines = Files.lines(propertiesFile, Charsets.ISO_8859_1)) {
        lines.forEach(line -> {
          if (line.contains("=")) {
            String key = line.substring(0, line.indexOf("=")).trim();
            String value = properties.getProperty(key);
            if (value != null) {
              writer.println(key + "=" + value);
            } else {
              writer.println(line);
            }
          } else {
            writer.println(line);
          }
        });
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    try {
      FileUtil.copyFile(tmpPath.toFile(), propertiesFile.toFile());
      Files.deleteIfExists(tmpPath);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Path toFileSystemPath(String resourcePath) {
    int idx = resourcePath.lastIndexOf('.');
    String[] nodes = resourcePath.substring(0, idx).split("\\.");
    Path configRootPath = ResourceLocator.getResourcesRootPath();
    Path path = configRootPath.resolve(
        Stream.of(nodes)
            .collect(Collectors.joining(configRootPath.getFileSystem().getSeparator())));
    return path.resolve(resourcePath.substring(idx + 1) + ".properties");
  }

}
  