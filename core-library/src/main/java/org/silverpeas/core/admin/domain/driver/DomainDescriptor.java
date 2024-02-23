package org.silverpeas.core.admin.domain.driver;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.SilverpeasResourcesLocation;
import org.silverpeas.kernel.annotation.NonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Descriptor of a domain. It is made up of two properties file: one describing the domain itself,
 * how to access it and some additional properties, and the other one defining the authentication
 * servers to use when authenticating a user of the domain.
 *
 * @author mmoquillon
 */
public class DomainDescriptor {

  private final Path domainPath;
  private final Path authPath;

  private final Properties domainProperties = new Properties();
  private final Properties authProperties = new Properties();

  /**
   * Constructs a new descriptor of a domain with the specified name. The name is used to find the
   * different properties file of the domain.
   *
   * @param domainName the name of a domain.
   */
  public DomainDescriptor(final String domainName) {
    Path silverpeasConfigRoot =
        SilverpeasResourcesLocation.getInstance().getConfigurationFilesRootPath();
    this.domainPath = silverpeasConfigRoot.resolve(Path.of("org", "silverpeas", "domains",
        "domain" + domainName + ".properties"));
    this.authPath = silverpeasConfigRoot.resolve(Path.of("org", "silverpeas", "authentication",
        "autDomain" + domainName + ".properties"));
  }

  /**
   * Loads only the properties describing the domain itself.
   *
   * @throws UncheckedIOException if an error occurs while loading the domain properties.
   */
  public void loadDomainProperties() throws UncheckedIOException {
    try (Reader reader = Files.newBufferedReader(this.domainPath)) {
      this.domainProperties.load(reader);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Loads only the authentication properties of the domain.
   *
   * @throws UncheckedIOException if an error occurs while loading the authentication properties.
   */
  public void loadAuthenticationProperties() throws UncheckedIOException {
    try (Reader reader = Files.newBufferedReader(this.authPath)) {
      this.authProperties.load(reader);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Gets the properties defining the domain itself and its parameters. It the properties were not
   * previously loaded, then the returned properties will be empty.
   *
   * @return the domain properties.
   */
  @NonNull
  public Properties getDomainProperties() {
    return domainProperties;
  }

  /**
   * Gets the authentication properties of the domain. It the properties were not previously loaded,
   * then the returned properties will be empty.
   *
   * @return the authentication properties.
   */
  @NonNull
  public Properties getAuthenticationProperties() {
    return authProperties;
  }

  /**
   * Updates the domain properties with the specified ones. New properties are ignored; only
   * existing properties are updated with their new value.
   * @param domainProperties the new valued properties with which the existing ones will be updated.
   * @throws UncheckedIOException if an error occurs while storing the new properties value into the
   * properties file.
   */
  public void updateDomainProperties(final Properties domainProperties)
      throws UncheckedIOException {
    if (domainProperties != null && !domainProperties.isEmpty()) {
      update(this.domainPath, domainProperties);
    }
  }

  /**
   * Updates the authentication properties with the specified ones. New properties are ignored;
   * only existing properties are updated with their new value.
   * @param authProperties the new valued properties with which the existing ones will be updated.
   * @throws UncheckedIOException if an error occurs while storing the new properties value into the
   * properties file.
   */
  public void updateAuthenticationProperties(final Properties authProperties)
      throws UncheckedIOException {
    if (authProperties != null && !authProperties.isEmpty()) {
      update(this.authPath, authProperties);
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

}
  