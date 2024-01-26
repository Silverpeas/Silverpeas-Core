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

package org.silverpeas.core.persistence;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Permits to set easily a specific dialect to use instead of using a detected one.
 * @author silveryocha
 */
public class SilverpeasDialectResolver implements DialectResolver {
  private static final long serialVersionUID = 901024770482418345L;

  @Override
  @SuppressWarnings("unchecked")
  public Dialect resolveDialect(DialectResolutionInfo info) {
    final String dialect = System.getProperty("silverpeas.jpa.dialect");
    if (isDefined(dialect)) {
      final Logger logger = Logger.getLogger(Dialect.class.getName());
      try {
        final Class<Dialect> dialectClass = (Class<Dialect>) Class.forName(dialect);
        Constructor<Dialect> constructor = dialectClass.getConstructor();
        final Dialect dialectInstance = constructor.newInstance();
        logger.log(Level.SEVERE, "SilverpeasDialectResolver - Using dialect: {0}",
            dialectClass.getName());
        return dialectInstance;
      } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
               ClassNotFoundException | InvocationTargetException | ClassCastException e) {
        logger.severe(e.getMessage());
      }
    }
    return null;
  }
}
