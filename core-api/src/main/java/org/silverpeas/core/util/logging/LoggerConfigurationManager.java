/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.util.logging;

import org.silverpeas.core.annotation.Module;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.lang.SystemWrapper;

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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * There is a single global LoggerConfigurationManager object that is used to manage a set of
 * configuration about the different Loggers available in Silverpeas.
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
  private static final String DEFAULT_NAMESPACE = "Silverpeas.Other.{0}";
  private static final String NO_SUCH_LOGGER_CONFIGURATION =
      "No such logger {0} defined for Silverpeas module {1}";
  private static final int INITIAL_CAPACITY = 128;

  private static Map<String, Properties> confByModule = new ConcurrentHashMap<>(INITIAL_CAPACITY);
  private static Map<String, Properties> confByLogger = new ConcurrentHashMap<>(INITIAL_CAPACITY);

  private static File getConfigurationHome() {
    Path path =
        Paths.get(SystemWrapper.get().getenv("SILVERPEAS_HOME"), "properties", "org", "silverpeas",
            "util", "logging");
    return path.toFile();
  }

  protected LoggerConfigurationManager() {
  }

  protected Map<String, Properties> getLoggerConfigurationsByModule() {
    return confByModule;
  }

  protected Map<String, Properties> getLoggerConfigurationsByLogger() {
    return confByLogger;
  }

  @PostConstruct
  protected void loadAllConfigurationFiles() {
    java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
        .log(java.util.logging.Level.INFO, "Silverpeas Logging Engine initialization...");
    File configurationHome = getConfigurationHome();
    File[] configurationFiles =
        configurationHome.listFiles((dir, name) -> name.endsWith(".properties"));
    confByModule.clear();
    confByLogger.clear();
    if (configurationFiles != null) {
      for (File aConfigurationFile : configurationFiles) {
        Properties properties = new Properties();
        try {
          properties.load(new FileInputStream(aConfigurationFile));
          properties.setProperty(CONFIGURATION_PATH, aConfigurationFile.getAbsolutePath());
          confByModule.put(properties.getProperty(SILVERPEAS_MODULE), properties);
          confByLogger.put(properties.getProperty(LOGGER_NAMESPACE), properties);
        } catch (IOException e) {
          java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
              .log(java.util.logging.Level.WARNING, e.getMessage());
        }
      }
    } else {
      java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
          .log(java.util.logging.Level.WARNING,
              "No logging configuration files found for Silverpeas");
    }
  }

  public static LoggerConfigurationManager get() {
    return ServiceProvider.getService(LoggerConfigurationManager.class);
  }

  /**
   * Gets the configuration parameters for the logger with the specified namespace or mapped with
   * the specified Silverpeas module.
   * @param moduleOrNamespace either a logger namespace or the name of a Silverpeas module. A Silverpeas
   * module is either a component of Silverpeas Core or an application in Silverpeas Components.
   * @return the configuration of the logger defined for the specified Silverpeas module or by
   * the specified namespace.
   */
  public LoggerConfiguration getLoggerConfiguration(String moduleOrNamespace) {
    Properties properties = getLoggerConfigurationsByModule().get(moduleOrNamespace);
    if (properties == null) {
      properties = getLoggerConfigurationsByLogger().get(moduleOrNamespace);
    }
    return loadLoggerConfiguration(properties, moduleOrNamespace);
  }

  /**
   * Gets the configuration parameters for the logger mapped with the Silverpeas module that
   * includes the specified Java package.
   * </p>
   * By convention, each Silverpeas module matches a given package node by the name and all
   * the children of this package node are then included by the module. So, to find the logger
   * mapped to a module, we have to seek the module whose the name either matches one of the node
   * of the specified package or is provided by the {@link org.silverpeas.core.annotation.Module}}
   * annotation of one of the node of the specified package.
   * @param aPackage a Java package/
   * @return the configuration of the logger defined for the Silverpeas module matching the
   * specified package.
   */
  public LoggerConfiguration getLoggerConfiguration(Package aPackage) {
    String path = aPackage.getName();
    String module = path;
    for (int i = path.lastIndexOf("."); i > 0 && !module.equals("silverpeas");
         i = path.lastIndexOf(".")) {
      Package p = Package.getPackage(path);
      Module m =
          (p == null ? null : p.getAnnotation(Module.class)); // package-info.java can be missed
      if (m == null) {
        module = path.substring(i + 1);
        if (!confByLogger.containsKey(module)) {
          path = path.substring(0, i);
        }
      } else {
        module = m.value();
        break;
      }
    }
    Properties properties = getLoggerConfigurationsByModule().get(module);
    return loadLoggerConfiguration(properties, module);
  }

  /**
   * Saves the configuration of the logger referred by the specified configuration instance.
   * Disclaimer: the change of the configuration isn't applied to the concerned logger, it has
   * to be done explicitly.
   * <p>
   * If no configuration exists for the logger referred by the configuration object, then nothing
   * is done.
   * @param configuration the new configuration of the logger defined for the specified Silverpeas
   * module.
   */
  public void saveLoggerConfiguration(LoggerConfiguration configuration) {
    Map<String, Properties> loggerConfigurations = getLoggerConfigurationsByModule();
    if (loggerConfigurations.containsKey(configuration.getModuleName())) {
      Properties properties = loggerConfigurations.get(configuration.getModuleName());
      String configurationPath = properties.getProperty(CONFIGURATION_PATH);
      if (configuration.getLevel() == null) {
        properties.remove(LOGGER_LEVEL);
      } else {
        properties.setProperty(LOGGER_LEVEL, configuration.getLevel().name());
      }
      properties.remove(CONFIGURATION_PATH);
      try {
        properties.store(new FileOutputStream(new File(configurationPath)), null);
      } catch (IOException e) {
        java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
            .log(java.util.logging.Level.WARNING, e.getMessage());
      } finally {
        properties.setProperty(CONFIGURATION_PATH, configurationPath);
      }
    } else {
      java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
          .log(java.util.logging.Level.WARNING, NO_SUCH_LOGGER_CONFIGURATION,
              new String[] {configuration.getNamespace(), configuration.getModuleName()});
    }
  }

  /**
   * Gets the available configuration of all the Silverpeas loggers. If a logger has no
   * defined configuration, then it is'nt taken into account.
   * @return a set of logger configurations sorted by the logger's namespace.
   */
  public Set<LoggerConfiguration> getAvailableLoggerConfigurations() {
    Set<String> moduleNames = getLoggerConfigurationsByModule().keySet();
    Set<LoggerConfiguration> availableConfigurations = new TreeSet<>();
    for (String module: moduleNames) {
      LoggerConfiguration configuration = getLoggerConfiguration(module);
      availableConfigurations.add(configuration);
    }
    return availableConfigurations;
  }

  public static class LoggerConfiguration implements Comparable<LoggerConfiguration> {
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

    @Override
    public int compareTo(final LoggerConfiguration other) {
      return this.getNamespace().compareTo(other.getNamespace());
    }
  }

  private LoggerConfiguration loadLoggerConfiguration(Properties properties, String defaultModule) {
    String namespace =
        Character.toString(defaultModule.charAt(0)).toUpperCase() + defaultModule.substring(1);
    String module = defaultModule;
    Level level = null;
    if (properties != null) {
      namespace = properties.getProperty(LOGGER_NAMESPACE);
      module = properties.getProperty(SILVERPEAS_MODULE);
      if (namespace == null || namespace.trim().isEmpty()) {
        namespace = MessageFormat.format(DEFAULT_NAMESPACE, module);
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
    } else if (!namespace.startsWith(SilverLogger.ROOT_NAMESPACE)) {
      // in the case it isn't a namespace dedicated to Silverpeas or it is a module name
      namespace = MessageFormat.format(DEFAULT_NAMESPACE, defaultModule);
    }
    return new LoggerConfiguration(module, namespace).withLevel(level);
  }
}
