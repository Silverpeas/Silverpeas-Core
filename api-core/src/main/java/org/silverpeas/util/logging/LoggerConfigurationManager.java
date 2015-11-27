/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.util.logging;

import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.lang.SystemWrapper;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * There is a single global LoggerConfigurationManager object that is used to manage a set of
 * configuration about the different Loggers available in Silverpeas. The
 * LoggerConfigurationManager single instance is managed by the underlying IoD container.
 * </p>
 * Each logger in Silverpeas is usually mapped to a given Silverpeas module. A Silverpeas module
 * is either a component of Silverpeas Core or an application in Silverpeas Components. By defining
 * a mapping between a module and a logger, when logging some messages, it is required to the
 * objects just to pass the name of the module to which they belong; it is hence no necessary to
 * remind the schema of the loggers namespace in use in Silverpeas. Each mapping is defined in a
 * logging configuration with optionally the logging level to use for the mapped logger.
 * </p>
 * A logging configuration for a Silverpeas module is stored into a properties file that must
 * be located in the <code>SILVERPEAS_HOME/properties/org/silverpeas/util/logging</code> directory.
 * @author miguel
 */
@Singleton
public class LoggerConfigurationManager {

  private static final String THIS_LOGGER_NAMESPACE = "Silverpeas.Core.Logging";
  private static final String SILVERPEAS_MODULE = "module.name";
  private static final String LOGGER_NAMESPACE = "module.logger";
  private static final String LOGGER_LEVEL = "module.level";
  private static final String CONFIGURATION_PATH = "configuration.path";
  private static final String DEFAULT_NAMESPACE = "Silverpeas.Util.{0}";
  private static final String NO_SUCH_LOGGER_CONFIGURATION =
      "No such logger {0} defined for Silverpeas module {1}";

  private static Map<String, Properties> configurations;

  private static File getConfigurationHome() {
    Path path =
        Paths.get(SystemWrapper.get().getenv("SILVERPEAS_HOME"), "properties", "org", "silverpeas",
            "util", "logging");
    return path.toFile();
  }

  protected Map<String, Properties> getLoggerConfigurations() {
    return configurations;
  }

  @PostConstruct
  protected void loadAllConfigurationFiles() {
    File configurationHome = getConfigurationHome();
    File[] configurationFiles =
        configurationHome.listFiles((dir, name) -> name.endsWith(".properties"));
    if (configurationFiles != null) {
      if (configurations != null) {
        configurations.clear();
      } else {
        configurations = new ConcurrentHashMap<>(configurationFiles.length);
      }
      for (File aConfigurationFile : configurationFiles) {
        Properties properties = new Properties();
        try {
          properties.load(new FileInputStream(aConfigurationFile));
          properties.setProperty(CONFIGURATION_PATH, aConfigurationFile.getAbsolutePath());
          configurations.put(properties.getProperty(SILVERPEAS_MODULE), properties);
        } catch (IOException e) {
          java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
              .log(java.util.logging.Level.WARNING, e.getMessage());
        }
      }
    }
  }

  public static LoggerConfigurationManager get() {
    return ServiceProvider.getService(LoggerConfigurationManager.class);
  }

  /**
   * Gets the configuration parameters for the logger mapped with the specified Silverpeas module.
   * @param module the name of the Silverpeas module. A Silverpeas module is either a component of
   * Silverpeas Core or an application in Silverpeas Components.
   * @return the configuration of the logger defined for the specified Silverpeas module.
   */
  public LoggerConfiguration getLoggerConfiguration(String module) {
    String namespace;
    Map<String, Properties> loggerConfigurations = getLoggerConfigurations();
    Level level = null;
    if (loggerConfigurations.containsKey(module)) {
      Properties properties = loggerConfigurations.get(module);
      namespace = properties.getProperty(LOGGER_NAMESPACE);
      if (namespace == null || namespace.trim().isEmpty()) {
        String suffix = Character.toUpperCase(module.charAt(0)) + module.substring(1);
        namespace = MessageFormat.format(DEFAULT_NAMESPACE, suffix);
      }
      String strLevel = properties.getProperty(LOGGER_LEVEL);
      if (strLevel != null && !strLevel.trim().isEmpty()) {
        try {
          level = Level.valueOf(strLevel);
        } catch (Throwable t) {
          java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
              .log(java.util.logging.Level.SEVERE, t.getMessage(), t);
        }
      }
    } else {
      String suffix = Character.toUpperCase(module.charAt(0)) + module.substring(1);
      namespace = MessageFormat.format(DEFAULT_NAMESPACE, suffix);
    }
    return new LoggerConfiguration(module, namespace).withLevel(level);
  }

  /**
   * Updates the configuration of the logger referred by the specified configuration instance.
   * <p>
   * If no configuration exists for the logger referred by the configuration object, then nothing
   * is done.
   * @param configuration the new configuration of the logger defined for the specified Silverpeas
   * module.
   */
  public void updateLoggerConfiguration(LoggerConfiguration configuration) {
    Map<String, Properties> loggerConfigurations = getLoggerConfigurations();
    if (loggerConfigurations.containsKey(configuration.getModuleName())) {
      Properties properties = loggerConfigurations.get(configuration.getModuleName());
      String configurationPath = (String) properties.remove(CONFIGURATION_PATH);
      properties.setProperty(LOGGER_LEVEL, configuration.getLevel().name());
      try {
        properties.store(new FileOutputStream(new File(configurationPath)), null);
      } catch (IOException e) {
        java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
            .log(java.util.logging.Level.WARNING, e.getMessage());
      }
      properties.setProperty(CONFIGURATION_PATH, configurationPath);
    } else {
      java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
          .log(java.util.logging.Level.WARNING, NO_SUCH_LOGGER_CONFIGURATION,
              new String[] {configuration.getNamespace(), configuration.getModuleName()});
    }
  }

  protected LoggerConfigurationManager() {

  }

  public static class LoggerConfiguration {
    private final String module;
    private final String namespace;
    private Level level;

    public LoggerConfiguration(String module, String namespace) {
      this.module = module;
      this.namespace = namespace;
    }

    public LoggerConfiguration withLevel(final Level level) {
      setLevel(level);
      return this;
    }

    public String getModuleName() {
      return module;
    }

    public String getNamespace() {
      return namespace;
    }

    public Level getLevel() {
      return level;
    }

    public void setLevel(final Level level) {
      this.level = level;
    }

  }
}
