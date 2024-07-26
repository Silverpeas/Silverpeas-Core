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
package org.silverpeas.core.persistence.jdbc.bean;

import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.kernel.annotation.NonNull;

import java.io.Serializable;

/**
 * SilverpeasBean represents an entity in the old silverpeas persistence layer (before JPA). All
 * the entity beans have to extends this abstract class.
 *
 * @deprecated
 */
@Deprecated
public abstract class SilverpeasBean implements SilverpeasEntityBean, Serializable {

  private static final long serialVersionUID = -7843189803570333207L;
  private WAPrimaryKey pk;

  public SilverpeasBean() {
    pk = new IdPK();
  }

  @Override
  public WAPrimaryKey getPK() {
    return pk;
  }

  @Override
  public void setPK(WAPrimaryKey value) {
    pk = value;
  }

  /**
   * Gets the name of the table in the database in which the bean is persisted and retrieved.
   * This method is to be used by SivlerpeasBeanDAO implementation for its persistence tasks.
   * @return the name of the SQL table.
   */
  @NonNull
  protected abstract String getTableName();

  /**
   * Ensures the specified string is sure. For doing, if it is null, then an empty string is
   * returned instead.
   * @param theString the string to ensure it is not null.
   * @return either the specified string or an empty string otherwise.
   */
  protected String getNonNullString(String theString) {
    return (theString == null) ? "" : theString;
  }
}
