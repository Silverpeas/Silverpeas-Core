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
package org.silverpeas.core.webapi.util.logging;

import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.LoggerConfigurationManager.LoggerConfiguration;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Arrays;

/**
 * Web entity used to carries the configuration of a given logger between a client and a REST
 * resource.
 * @author mmoquillon
 */
public class LoggerConfigurationEntity implements WebEntity {

  /**
   * Constant that defines the level of a logger is the one of one of its parent. It is defaulted
   * to one of its parent logger.
   */
  public static String PARENT_LEVEL = "PARENT";

  @XmlElement(defaultValue = "")
  private URI uri;
  private String logger;
  private String level;

  private LoggerConfigurationEntity() {
  }

  private LoggerConfigurationEntity(String logger, String level) {
    this.logger = logger;
    this.level = (level == null || level.trim().isEmpty() ? PARENT_LEVEL : level);
  }

  /**
   * Constructs from the specified logger configuration its Web entity representation ready to
   * be serialized in a given format (MIME type).
   * @param configuration a logger configuration.
   * @return the Web entity representation of the logger configuration.
   */
  public static LoggerConfigurationEntity toWebEntity(LoggerConfiguration configuration) {
    String level =
        (configuration.getLevel() == null ? PARENT_LEVEL : configuration.getLevel().name());
    return new LoggerConfigurationEntity(configuration.getNamespace(), level);
  }

  public LoggerConfigurationEntity withAsURi(URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Gets the URI at which this web entity is published and can be accessed.
   * @return the web entity URI.
   */
  @Override
  public URI getURI() {
    return uri;
  }

  public String getLogger() {
    return logger;
  }

  public String getLevel() {
    return level;
  }

  public LoggerConfiguration toLoggerConfiguration() {
    Level loggingLevel = (isLevelDefined(getLevel()) ? Level.valueOf(getLevel()) : null);
    return LoggerConfigurationManager.get()
        .getLoggerConfiguration(getLogger())
        .withLevel(loggingLevel);
  }

  private boolean isLevelDefined(String level) {
    return Arrays.asList(Level.values())
        .stream()
        .filter(l -> l.name().equals(level))
        .findFirst()
        .isPresent();
  }
}
