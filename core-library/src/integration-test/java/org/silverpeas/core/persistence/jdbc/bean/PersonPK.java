/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception.  You should have received a copy of the text describi
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jdbc.bean;

import org.silverpeas.core.WAPrimaryKey;

import java.util.Objects;

/**
 * @author mmoquillon
 */
public class PersonPK extends WAPrimaryKey {

  public PersonPK(String id) {
    super(id);
  }

  public PersonPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public PersonPK(String id, String componentId) {
    super(id, componentId);
  }

  public PersonPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  @Override
  public String getRootTableName() {
    return "Person";
  }

  @Override
  public String getTableName() {
    return "SB_Person";
  }

  @Override
  public boolean equals(Object obj) {
    return Objects.equals(this, obj);
  }
}
  