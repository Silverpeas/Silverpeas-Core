package org.silverpeas.core.util;

import org.silverpeas.kernel.SilverpeasResourcesLocation;
import org.silverpeas.kernel.util.SystemWrapper;

import java.nio.file.Path;

/**
 * Locator of the root directories of each resources type used by Silverpeas. These resources are
 * the loggers configuration files, the L10n bundles and the Silverpeas properties files.
 * @author mmoquillon
 */
public class ResourcesRootDirectoriesLocator implements SilverpeasResourcesLocation {
  private final String silverpeasHome = SystemWrapper.getInstance().getenv("SILVERPEAS_HOME");
  private final Path propertiesHome = Path.of(silverpeasHome, "properties");

  private final Path loggersHome = propertiesHome
      .resolve(Path.of("org", "silverpeas", "util", "logging"));

  @Override
  public Path getLoggersRootPath() {
    return loggersHome;
  }

  @Override
  public Path getL10nBundlesRootPath() {
    return getConfigurationFilesRootPath();
  }

  @Override
  public Path getConfigurationFilesRootPath() {
    return propertiesHome;
  }
}
  