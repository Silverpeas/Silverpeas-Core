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
package org.silverpeas.core.util.logging.sys;

import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.LoggerConfigurationManager.LoggerConfiguration;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.logging.SilverLoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Map;

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

  private static Map<String, WeakLoggerReference> loggers = new Hashtable<>();

  @Override
  public SilverLogger getLogger(final String namespace, LoggerConfiguration configuration) {
    WeakLoggerReference weakRef = loggers.get(namespace);
    if (weakRef == null || weakRef.get() == null) {
      if (weakRef != null) {
        loggers.remove(weakRef.getNamespace());
      }

      weakRef = loggers.computeIfAbsent(namespace, n -> {
        SysLogger logger = new SysLogger(namespace);
        if (!configuration.getNamespace().equals(SilverLogger.ROOT_NAMESPACE) ||
            configuration.getLevel() != null) {
          // we take care to not erase the root logger level
          logger.setLevel(configuration.getLevel());
        }
        return new WeakLoggerReference(logger);
      });
    }
    return weakRef.get();
  }

  private static class WeakLoggerReference extends WeakReference<SilverLogger> {

    private final String namespace;

    /**
     * Creates a new weak reference that refers to the given object.  The new
     * reference is not registered with any queue.
     * @param referent object the new weak reference will refer to
     */
    public WeakLoggerReference(final SilverLogger referent) {
      super(referent);
      this.namespace = referent.getNamespace();
    }

    public String getNamespace() {
      return namespace;
    }
  }

}
