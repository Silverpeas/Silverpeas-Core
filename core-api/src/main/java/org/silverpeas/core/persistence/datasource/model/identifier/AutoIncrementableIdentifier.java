/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.datasource.model.identifier;

import jakarta.persistence.MappedSuperclass;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.jdbc.DBUtil;

/**
 * An entity identifier with an automatic value incrementation capability by using the Silverpeas
 * mechanism for that. For doing, the concrete value of the identifier has to be a numeric value.
 *
 * @author mmoquillon
 */
@MappedSuperclass
public abstract class AutoIncrementableIdentifier<T extends Number> extends BaseEntityIdentifier<T>
    implements EntityIdentifier {

  protected int nextNewValue(String... parameters) {
    final String tableName = parameters[0];
    final String tableColumnIdName = parameters[1];
    return DBUtil.getNextId(tableName, tableColumnIdName);
  }

}
  