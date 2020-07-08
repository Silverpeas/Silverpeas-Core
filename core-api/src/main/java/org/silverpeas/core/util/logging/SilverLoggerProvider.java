/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.logging;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;

/**
 * A provider of a {@link SilverLogger}. It is managed by the underlying IoD subsystem. It is used
 * by the {@link SilverLogger} class to get a {@link SilverLogger} instance matching a given logging
 * namespace. It can be also directly used in injection points instead of using directly the
 * {@link SilverLogger#getLogger(Object)} or {@link SilverLogger#getLogger(String)} methods.
 * Usually, the later is required for unmanaged beans whereas the foremost can be used by the
 * managed beans.
 * @author mmoquillon
 */
@Provider
public class SilverLoggerProvider {

  /**
   * The namespace of the root logger in Silverpeas. This namespace is predefined and each logger
   * taken in charge by the Logging Engine should be a descendant of its logger.
   */
  public static final String ROOT_NAMESPACE = "silverpeas";

  private LoggerConfigurationManager configurationManager;
  private SilverLoggerFactory loggerFactory = SilverLoggerFactory.get();

  @Inject
  protected SilverLoggerProvider(final LoggerConfigurationManager loggerConfigurationManager) {
    this.configurationManager = loggerConfigurationManager;
  }

  /**
   * Gets an instance of a provider of {@link SilverLogger} objects.
   * @return a {@link SilverLoggerProvider} instance.
   */
  protected static SilverLoggerProvider getLoggerProvider() {
    return ServiceProvider.getSingleton(SilverLoggerProvider.class);
  }

  /**
   * Gets the logger that is defined for the specified logging namespace.
   * <p>
   * A logging namespace is the name or a category under which the messages will be log. The
   * messages will be logged only if the logging level satisfies the logger's level. If no level is
   * set for the logger, then the level from its nearest ancestor with a specific
   * (non-null) level value is taken into account.
   * </p>
   * The logger instance is obtained from the logging namespace by using the
   * {@link org.silverpeas.core.util.logging.SilverLoggerFactory} factory. The factory
   * has also the responsibility of the initialization mechanism of the logger from its logging
   * configuration parameters before returning it.
   * @param module a logging namespace.
   * @return a logger instance identified by the given logging namespace, manufactured by a
   * {@link SilverLoggerFactory} instance.
   */
  public SilverLogger getLogger(final String module) {
    LoggerConfigurationManager.LoggerConfiguration configuration =
        this.configurationManager.getLoggerConfiguration(module);
    return loggerFactory.getLogger(configuration.getNamespace(), configuration);
  }

  /**
   * Gets a logger for the specified object. The logger is found from the package name of the
   * specified object.
   * <p>
   * In Silverpeas, each logger namespace matches a package name, starting from the
   * <code>silverpeas</code> subpackage: for a package <code>org.silverpeas.core.io</code>, there
   * is a logger with the namespace <code>silverpeas.core.io</code>.
   * </p>
   * @param object the object from which the logger namespace will be determined.
   * @return a logger instance identified by the logging namespace that was determined from the
   * given object, manufactured by a {@link SilverLoggerFactory} instance.
   * @see SilverLoggerProvider#getLogger(String)
   */
  public SilverLogger getLogger(Object object) {
    Package pkg =
        object instanceof Class ? ((Class) object).getPackage() : object.getClass().getPackage();
    String namespace = pkg.getName();
    if (namespace.startsWith("org.silverpeas")) {
      namespace = namespace.substring(namespace.indexOf('.') + 1);
    }
    Logger annotation = pkg.getAnnotation(Logger.class);
    if (annotation == null && object instanceof Class) {
      annotation = (Logger) ((Class) object).getAnnotation(Logger.class);
    }
    if (annotation != null) {
      namespace = annotation.value();
    }
    return getLogger(namespace);
  }
}
  