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
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;

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

  private static final String LOGGER_CONF_FILE_SUFFIX = "Logging.properties";
  private static final String THIS_LOGGER_NAMESPACE = "silverpeas.core.logging";
  private static final String LOGGER_NAMESPACE = "namespace";
  private static final String LOGGER_LEVEL = "level";
  private static final String NO_SUCH_LOGGER_CONFIGURATION =
      "No such logger {0} defined for Silverpeas";
  private static final int INITIAL_CAPACITY = 128;

  private static Map<String, LoggerConfiguration> configs =
      new ConcurrentHashMap<>(INITIAL_CAPACITY);

  private static File getConfigurationHome() {
    Path path =
        Paths.get(SystemWrapper.get().getenv("SILVERPEAS_HOME"), "properties", "org", "silverpeas",
            "util", "logging");
    return path.toFile();
  }

  protected LoggerConfigurationManager() {
  }

  protected Map<String, LoggerConfiguration> getLoggerConfigurations() {
    return configs;
  }

  @PostConstruct
  protected void loadAllConfigurationFiles() {
    java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
        .log(java.util.logging.Level.INFO, "Silverpeas Logging Engine initialization...");
    File configurationHome = getConfigurationHome();
    File[] configurationFiles =
        configurationHome.listFiles((dir, name) -> name.endsWith(LOGGER_CONF_FILE_SUFFIX));
    configs.clear();
    if (configurationFiles != null) {
      for (File aConfigurationFile : configurationFiles) {
        try {
          LoggerConfiguration loggerConfiguration = loadLoggerConfiguration(aConfigurationFile);
          configs.put(loggerConfiguration.getNamespace(), loggerConfiguration);
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
   * Gets the configuration parameters for the logger with the specified namespace.
   * @param namespace a logger namespace.
   * @return the configuration of the logger defined for the specified namespace.
   */
  public LoggerConfiguration getLoggerConfiguration(String namespace) {
    Map<String, LoggerConfiguration> loggerConfigurations = getLoggerConfigurations();
    return loggerConfigurations.computeIfAbsent(namespace, ns -> new LoggerConfiguration(null, ns));
  }

  /**
   * Saves the configuration of the logger referred by the specified configuration instance.
   * <p>
   * If no configuration exists for the logger referred by the configuration object, then nothing
   * is done.
   * @param configuration the new configuration of the logger defined for the specified Silverpeas
   * module.
   */
  public void saveLoggerConfiguration(LoggerConfiguration configuration) {
    Map<String, LoggerConfiguration> loggerConfigurations = getLoggerConfigurations();
    if (loggerConfigurations.containsKey(configuration.getNamespace()) &&
        configuration.hasConfigurationFile()) {
      try {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configuration.getConfigurationFile()));
        if (configuration.getLevel() == null) {
          properties.remove(LOGGER_LEVEL);
        } else {
          properties.setProperty(LOGGER_LEVEL, configuration.getLevel().name());
        }
        properties.store(new FileOutputStream(configuration.getConfigurationFile()), null);
      } catch (IOException e) {
        java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
            .log(java.util.logging.Level.WARNING, e.getMessage());
      }
    }
  }

  /**
   * Gets the available configuration of all the Silverpeas loggers. If a logger has no
   * configuration file, then it is'nt taken into account.
   * @return a set of logger configurations sorted by logger namespace.
   */
  public Set<LoggerConfiguration> getAvailableLoggerConfigurations() {
    Collection<LoggerConfiguration> allConfigurations = getLoggerConfigurations().values();
    return allConfigurations.stream()
        .filter(LoggerConfiguration::hasConfigurationFile)
        .collect(Collector.of(TreeSet::new, TreeSet::add, (left, right) -> {
          left.addAll(right);
          return left;
        }));
  }

  private LoggerConfiguration loadLoggerConfiguration(File loggerConfFile) throws IOException {
    Properties loggerProperties = new Properties();
    loggerProperties.load(new FileInputStream(loggerConfFile));
    String namespace = loggerProperties.getProperty(LOGGER_NAMESPACE);
    Level level = null;
    String strLevel = loggerProperties.getProperty(LOGGER_LEVEL);
    if (strLevel != null && !strLevel.trim().isEmpty()) {
      try {
        level = Level.valueOf(strLevel);
      } catch (Throwable t) {
        java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
            .log(java.util.logging.Level.SEVERE, t.getMessage(), t);
      }
    }
    return new LoggerConfiguration(loggerConfFile, namespace).withLevel(level);
  }

  public static class LoggerConfiguration implements Comparable<LoggerConfiguration> {
    private final String namespace;
    private final File file;
    private Level level;

    LoggerConfiguration(File configFile, String namespace) {
      this.file = configFile;
      this.namespace = namespace;
    }

    public LoggerConfiguration withLevel(final Level level) {
      setLevel(level);
      return this;
    }

    private File getConfigurationFile() {
      return file;
    }

    protected boolean hasConfigurationFile() {
      return file != null && file.exists() && file.isFile();
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

}
