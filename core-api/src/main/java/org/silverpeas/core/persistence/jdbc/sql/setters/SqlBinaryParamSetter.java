/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jdbc.sql.setters;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * A setter of SQL parameters of type binaries. The binary types it supports are {@link Byte},
 * {@link Clob} and {@link Blob}.
 * @author mmoquillon
 */
public class SqlBinaryParamSetter implements SqlTypedParameterSetter {

  @Override
  public List<Class<?>> getSupportedTypes() {
    return Arrays.asList(Byte.class, Blob.class, Clob.class);
  }

  @Override
  public void setParameter(final PreparedStatement statement, final int idx, final Object value)
      throws SQLException {
    if (value instanceof Byte) {
      statement.setByte(idx, (Byte) value);
    } else if (value instanceof Blob) {
      statement.setBlob(idx, (Blob) value);
    } else if (value instanceof Clob) {
      statement.setClob(idx, (Clob) value);
    } else {
      throw new IllegalArgumentException("Type not supported: " + value.getClass());
    }
  }
}
  