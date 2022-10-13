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
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A setter of parameters of a {@link PreparedStatement} instance according to the concrete type of
 * the value with which the parameter has to be set.
 * @author mmoquillon
 */
@Technical
@Bean
@Singleton
public class SqlStatementParameterSetter {

  private Map<Class<?>, SqlTypedParameterSetter> settersByType = new HashMap<>();

  /**
   * Registers all of the SQL parameter setters available in Silverpeas for later use.
   */
  @PostConstruct
  void registerAll() {
    Set<SqlTypedParameterSetter> setters =
        ServiceProvider.getAllServices(SqlTypedParameterSetter.class);
    for(SqlTypedParameterSetter aSetter : setters) {
      for (Class<?> type: aSetter.getSupportedTypes()) {
        settersByType.put(type, aSetter);
      }
    }
  }

  /**
   * Sets the nth parameter of the specified SQL statement with the given value. The setting is
   * really done by using the parameter setter that supports the concrete type of the value; that
   * is to say by the setter that knows how to set a SQL parameter of a given type.
   * @param statement a {@link PreparedStatement} instance representing the SQL request.
   * @param idx the index of the parameter in the SQL statement.
   * @param value the value with which will be set the parameter.
   * @throws SQLException if an error occurs while setting the parameter.
   */
  public void setParameter(final PreparedStatement statement, final int idx, final Object value)
      throws SQLException {
    SqlTypedParameterSetter setter = getSetterForType(value.getClass());
    if (setter != null) {
      setter.setParameter(statement, idx, value);
    } else {
      setObjectIdentifier(statement, idx, value);
    }
  }

  private void setObjectIdentifier(final PreparedStatement preparedStatement,
      final int paramIndex, final Object parameter) throws SQLException {
    try {
      Method idGetter = parameter.getClass().getDeclaredMethod("getId");
      String id = (String) idGetter.invoke(parameter);
      preparedStatement.setString(paramIndex, id);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          "SQL parameter type not handled: " + parameter.getClass(), e);
    }
  }

  private SqlTypedParameterSetter getSetterForType(final Class<?> type) {
    SqlTypedParameterSetter setter = settersByType.get(type);
    if (setter == null) {
      Class<?> superType = type.getSuperclass();
      if (superType != null) {
        setter = getSetterForType(superType);
      }
    }
    return setter;
  }
}
  