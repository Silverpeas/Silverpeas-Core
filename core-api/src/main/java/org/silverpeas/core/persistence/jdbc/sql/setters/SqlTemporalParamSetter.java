/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.persistence.jdbc.sql.setters;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;

/**
 * An abstract setter of SQL parameters of type temporal. It provides common features for more
 * concrete setter of temporal parameters.
 * @author mmoquillon
 */
@Technical
@Bean
public abstract class SqlTemporalParamSetter implements SqlTypedParameterSetter {

  protected Instant toInstant(final Object value) {
    try {
      if (value instanceof Instant) {
        return (Instant) value;
      }
      Method toInstant = value.getClass().getMethod("toInstant");
      return (Instant) toInstant.invoke(value);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          "Date or date time parameter expected. But is " + value.getClass(), e);
    }
  }

  protected void throwTypeNotSupported(final Class<?> type) {
    throw new IllegalArgumentException("Type not supported: " + type.getName());
  }
}
  