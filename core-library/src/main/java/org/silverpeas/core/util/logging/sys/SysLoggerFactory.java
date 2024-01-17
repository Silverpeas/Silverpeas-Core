/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.logging.sys;

import org.silverpeas.core.util.logging.LoggerConfigurationManager.LoggerConfiguration;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.logging.SilverLoggerFactory;
import org.silverpeas.core.util.logging.SilverLoggerProvider;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Implementation of the {@code org.silverpeas.core.util.logging.LoggerFactory} interface to provide
 * a logger wrapping the default Java logger.
 * </p>
 * It manages a cache of {@code org.silverpeas.core.util.logging.sys.SysLogger} instances that were
 * already previously asked. If a logger isn't in the cache, then it manufactures it,
 * initializes it from its logging configuration, and puts it into the cache.
 * @author miguel
 */
public class SysLoggerFactory implements SilverLoggerFactory {

  private final Map<SilverLoggerKey, SilverLogger> loggers = new WeakHashMap<>();

  @Override
  public SilverLogger getLogger(final String namespace, LoggerConfiguration configuration) {
    return loggers.computeIfAbsent(new SilverLoggerKey(namespace), n -> {
        SysLogger logger = new SysLogger(namespace);
      if (!configuration.getNamespace().equals(SilverLoggerProvider.ROOT_NAMESPACE) ||
          configuration.getLevel() != null) {
        // we take care to not erase the root logger level
        logger.setLevel(configuration.getLevel());
      }
      return logger;
    });
  }

  /*
   * A String cannot be weakly referred. So we wrap it by an object that can be  weakly referred
   * into the WeakHashMap cache.
   */
  private static class SilverLoggerKey {
    private final String namespace;

    private SilverLoggerKey(final String namespace) {
      Objects.requireNonNull(namespace);
      this.namespace = namespace;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof SilverLoggerKey)) {
        return false;
      }
      final SilverLoggerKey loggerKey = (SilverLoggerKey) o;
      return Objects.equals(namespace, loggerKey.namespace);
    }

    @Override
    public int hashCode() {
      return Objects.hash(namespace);
    }
  }
}
